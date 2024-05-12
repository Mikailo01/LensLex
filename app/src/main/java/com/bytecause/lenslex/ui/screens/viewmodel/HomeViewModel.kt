package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val fireStore: FirebaseFirestore,
    supportedLanguagesRepository: SupportedLanguagesRepository,
    auth: Authenticator
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

    private lateinit var fireStoreSnapShotListener: ListenerRegistration
    private var userId = auth.getAuth.currentUser?.uid

    private val getAllWordsFromFireStore: Flow<List<WordsAndSentences>> =
        callbackFlow {
            addSnapShotListener()

            fireStore
                .collection("users")
                .document(userId.toString())
                .collection("WordsAndSentences")
                .get()
                .addOnSuccessListener { snapShot ->
                    trySend(
                        snapShot.documents.map(::mapDocumentObject).toMutableList()
                            .sortedByDescending { it.timeStamp }
                    )
                }
            awaitClose { close() }
        }

    private val _uiState =
        MutableStateFlow(HomeState(profilePictureUrl = auth.getAuth.currentUser?.photoUrl.toString()))
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            getAllWordsFromFireStore,
            languageOptionFlow,
            supportedLanguages
        ) { words, selectedLang, supportedLanguages ->

            _uiState.update { state ->
                state.copy(
                    wordList = words.takeIf { it != state.wordList } ?: state.wordList,
                    selectedLanguage = selectedLang.takeIf { it != state.selectedLanguage } ?: state.selectedLanguage,
                    supportedLanguages = supportedLanguages.takeIf { it != state.supportedLanguages } ?: state.supportedLanguages,
                    isLoading = words != state.wordList
                )
            }
        }.launchIn(viewModelScope)
    }

    private var deletedItemsStack = emptyList<WordsAndSentences>()

    fun uiEventHandler(event: HomeUiEvent.NonDirect) {
        when (event) {
            is HomeUiEvent.OnIconStateChange -> {
                _uiState.update { it.copy(fabState = event.value) }
            }

            is HomeUiEvent.OnConfirmLanguageDialog -> {
                saveTranslationOption(event.value)
                _uiState.update { it.copy(showLanguageDialog = false) }
            }

            is HomeUiEvent.OnShowLanguageDialog -> {
                _uiState.update { it.copy(showLanguageDialog = event.value) }
            }

            is HomeUiEvent.OnDownloadLanguage -> {
                downloadModel(event.langCode)
            }

            is HomeUiEvent.OnRemoveLanguage -> {
                removeModel(event.langCode)
            }

            is HomeUiEvent.OnItemRemoved -> {
                addDeletedItemToStack(event.word)
                deleteWordFromFireStore(event.word.id)
                _uiState.update { it.copy(showUndoButton = true) }
            }

            HomeUiEvent.OnItemRestored -> {
                insertWordToFireStore(deletedItemsStack.last())
                removeDeletedItemFromStack()
                _uiState.update { it.copy(showUndoButton = deletedItemsStack.isNotEmpty()) }
            }
        }
    }

    fun showProgressBar(boolean: Boolean) {
        _uiState.update { it.copy(showProgressBar = boolean) }
    }

    private fun addWords(list: List<WordsAndSentences>) {
        _uiState.update {
            it.copy(wordList = list)
        }
    }


    private fun addSnapShotListener() {
        userId?.let { id ->
            fireStoreSnapShotListener = fireStore
                .collection("users")
                .document(id)
                .collection("WordsAndSentences")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val wordsList = mutableListOf<WordsAndSentences>()
                        for (doc in snapshot.documents) {
                            wordsList.add(mapDocumentObject(doc))
                        }

                        addWords(wordsList.sortedByDescending { it.timeStamp })
                    } else {
                        addWords(emptyList())
                    }
                }
        }
    }

    private fun mapDocumentObject(document: DocumentSnapshot): WordsAndSentences {
        return document.data?.let { field ->
            WordsAndSentences(
                id = document.id,
                word = field["word"] as String,
                languageCode = field["languageCode"] as String,
                translations = field["translations"] as Map<String, String>,
                timeStamp = field["timeStamp"] as Long
            )
        } ?: WordsAndSentences()
    }

    private fun insertWordToFireStore(word: WordsAndSentences) {
        userId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                fireStore
                    .collection("users")
                    .document(id)
                    .collection("WordsAndSentences")
                    .document(word.id)
                    .set(word)
            }
        }
    }

    private fun deleteWordFromFireStore(documentId: String) {
        userId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                fireStore
                    .collection("users")
                    .document(id)
                    .collection("WordsAndSentences")
                    .document(documentId)
                    .delete()
            }
        }
    }

    private fun addDeletedItemToStack(item: WordsAndSentences) {
        deletedItemsStack += item
    }

    private fun removeDeletedItemFromStack() {
        deletedItemsStack = deletedItemsStack
            .toMutableList()
            .apply {
                removeLast()
            }
    }

    private fun saveTranslationOption(language: SupportedLanguage) {
        viewModelScope.launch {
            userPrefsRepositoryImpl.saveTranslationOption(language.langCode)
            super.setLangOption(language = language)
        }
    }

    fun insertOrUpdateWordAndSentenceEntity(word: WordAndSentenceEntity) {
        viewModelScope.launch {
            wordsDatabaseRepository.insertOrUpdateWordAndSentenceEntity(word)
        }
    }

    val getAllWords: Flow<List<WordAndSentenceEntity>> =
        wordsDatabaseRepository.getAllWords

    fun deleteWordById(id: Long) {
        viewModelScope.launch {
            wordsDatabaseRepository.deleteWordById(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::fireStoreSnapShotListener.isInitialized) fireStoreSnapShotListener.remove()
    }
}