package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val wordsRepository: WordsRepository,
    private val textRecognitionRepository: TextRecognitionRepository,
    translationModelManager: TranslationModelManager,
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository,
    auth: Authenticator
) : BaseViewModel(userPrefsRepository, translationModelManager, supportedLanguagesRepository) {

    private val _uiState =
        MutableStateFlow(HomeState(profilePictureUrl = auth.getAuth().currentUser?.photoUrl.toString()))
    val uiState = _uiState.asStateFlow()

    private val _textResultChannel = Channel<List<String>>()
    val textResultChannel = _textResultChannel.receiveAsFlow()

    init {
        combine(
            wordsRepository.getWords(),
            languageOptionFlow,
            supportedLanguages
        ) { words, selectedLang, supportedLanguages ->

            _uiState.update { state ->
                state.copy(
                    wordList = words.takeIf { it != state.wordList } ?: state.wordList,
                    selectedLanguageOptions = selectedLang.takeIf { it != state.selectedLanguageOptions }
                        ?: state.selectedLanguageOptions,
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
            is HomeUiEvent.OnIconStateChange -> onIconStateChange(event.value)
            is HomeUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialogHandler(event.value)
            is HomeUiEvent.OnShowLanguageDialog -> onShowLanguageDialogHandler(event.value)
            is HomeUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is HomeUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is HomeUiEvent.OnItemRemoved -> onItemRemovedHandler(event.word)
            is HomeUiEvent.OnTextRecognition -> onTextRecognitionHandler(event.imagePaths)
            HomeUiEvent.OnItemRestored -> onItemRestoredHandler()
        }
    }

    fun resetImageTextless() {
        _uiState.update { it.copy(isImageTextless = false) }
    }

    private fun onShowLanguageDialogHandler(option: TranslationOption?) {
        _uiState.update { it.copy(showLanguageDialog = option) }
    }

    private fun onIconStateChange(boolean: Boolean) {
        _uiState.update { it.copy(fabState = boolean) }
    }

    private fun onItemRemovedHandler(word: WordsAndSentences) {
        addDeletedItemToStack(word)
        deleteWord(word.id)
        _uiState.update { it.copy(showUndoButton = true) }
    }

    private fun onItemRestoredHandler() {
        insertWord(deletedItemsStack.last())
        removeDeletedItemFromStack()
        _uiState.update { it.copy(showUndoButton = deletedItemsStack.isNotEmpty()) }
    }

    private fun onTextRecognitionHandler(uris: List<Uri>) {
        _uiState.update {
            it.copy(showProgressBar = true)
        }

        viewModelScope.launch {
            runTextRecognition(uris).firstOrNull()?.let { result ->
                _uiState.update { state ->
                    state.copy(showProgressBar = false, isImageTextless = result.isEmpty())
                }
                if (result.isNotEmpty()) {
                    _textResultChannel.trySend(result)
                }
            }
        }
    }

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

    private fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> =
        textRecognitionRepository.runTextRecognition(imagePaths)

    private fun onConfirmLanguageDialogHandler(language: TranslationOption) {
        saveTranslationOption(language)
        _uiState.update { it.copy(showLanguageDialog = null) }
    }

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
}