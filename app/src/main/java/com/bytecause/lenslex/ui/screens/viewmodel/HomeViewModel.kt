package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val wordsRepository: WordsRepository,
    private val userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository,
    auth: Authenticator
) : BaseViewModel(userPrefsRepository, supportedLanguagesRepository) {

    private val _uiState =
        MutableStateFlow(HomeState(profilePictureUrl = auth.getAuth().currentUser?.photoUrl.toString()))
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            wordsRepository.getWords(),
            languageOptionFlow,
            supportedLanguages
        ) { words, selectedLang, supportedLanguages ->

            _uiState.update { state ->
                state.copy(
                    wordList = words.takeIf { it != state.wordList } ?: state.wordList,
                    selectedLanguage = selectedLang.takeIf { it != state.selectedLanguage }
                        ?: state.selectedLanguage,
                    supportedLanguages = supportedLanguages.takeIf { it != state.supportedLanguages }
                        ?: state.supportedLanguages,
                    isLoading = words != state.wordList
                )
            }

            _uiState.update { state ->
                state.copy(isLoading = state.wordList != words)
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
                deleteWord(event.word.id)
                _uiState.update { it.copy(showUndoButton = true) }
            }

            HomeUiEvent.OnItemRestored -> {
                insertWord(deletedItemsStack.last())
                removeDeletedItemFromStack()
                _uiState.update { it.copy(showUndoButton = deletedItemsStack.isNotEmpty()) }
            }
        }
    }

    fun showProgressBar(boolean: Boolean) {
        _uiState.update { it.copy(showProgressBar = boolean) }
    }

    /* private fun addWords(list: List<WordsAndSentences>) {
         _uiState.update {
             it.copy(wordList = list)
         }
     }*/


    /*private fun addSnapShotListener() {
        viewModelScope.launch {
            firestoreRepository.getWords.collectLatest {
                addWords(it)
            }
        }
        /*userId?.let { id ->
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
        }*/
    }*/

    private fun insertWord(word: WordsAndSentences) {
        viewModelScope.launch {
            wordsRepository.addWord(word).firstOrNull()
        }
    }

    private fun deleteWord(documentId: String) {
        wordsRepository.deleteWord(documentId)
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
            userPrefsRepository.saveTranslationOption(language.langCode)
            super.setLangOption(language = language)
        }
    }

    /* fun insertOrUpdateWordAndSentenceEntity(word: WordAndSentenceEntity) {
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
     }*/
}