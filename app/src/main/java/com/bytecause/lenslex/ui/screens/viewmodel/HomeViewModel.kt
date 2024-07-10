package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.HomeUiEffect
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val wordsRepository: WordsRepository,
    private val textRecognitionRepository: TextRecognitionRepository,
    private val userRepository: UserRepository,
    private val userPrefsRepository: UserPrefsRepository,
    translationModelManager: TranslationModelManager,
    supportedLanguagesRepository: SupportedLanguagesRepository,
) : TranslationViewModel(
    userPrefsRepository,
    translationModelManager,
    supportedLanguagesRepository
) {

    private val _uiState =
        MutableStateFlow(
            HomeState(
                profilePictureUrl = userRepository.getUserData()?.profilePictureUrl ?: "",
            )
        )
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private val _textResultChannel = Channel<List<String>>()
    val textResultChannel = _textResultChannel.receiveAsFlow()

    init {
        combine(
            wordsRepository.getWords(),
            languageOptionFlow,
            supportedLanguages,
        ) { words, selectedLang, supportedLanguages ->
            Triple(words, selectedLang, supportedLanguages)
        }.distinctUntilChanged().onEach { (words, selectedLang, supportedLanguages) ->
            _uiState.update { state ->
                state.copy(
                    wordList = words.takeIf { it != state.wordList } ?: state.wordList,
                    selectedLanguageOptions = selectedLang.takeIf { it != state.selectedLanguageOptions }
                        ?: state.selectedLanguageOptions,
                    supportedLanguages = supportedLanguages.takeIf { it != state.supportedLanguages }
                        ?: state.supportedLanguages,
                )
            }

            _uiState.update { it.copy(isLoading = it.wordList != words) }
        }.launchIn(viewModelScope)

        /* combine(
             wordsRepository.getWords(),
             languageOptionFlow,
             supportedLanguages,
         ) { words, selectedLang, supportedLanguages ->
             _uiState.update { state ->
                 state.copy(
                     wordList = words.takeIf { it != state.wordList } ?: state.wordList,
                     selectedLanguageOptions = selectedLang.takeIf { it != state.selectedLanguageOptions }
                         ?: state.selectedLanguageOptions,
                     supportedLanguages = supportedLanguages.takeIf { it != state.supportedLanguages }
                         ?: state.supportedLanguages,
                     isLoading = words != state.wordList,
                 )
             }

             _uiState.update { it.copy(isLoading = it.wordList != words) }
         }.launchIn(viewModelScope)*/
    }

    private var deletedItemsStack = emptyList<WordsAndSentences>()

    fun uiEventHandler(event: HomeUiEvent.NonDirect) {
        when (event) {
            is HomeUiEvent.OnIconStateChange -> onIconStateChange(event.value)
            is HomeUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialog(event.value)
            is HomeUiEvent.OnShowLanguageDialog -> onShowLanguageDialog(event.value)
            is HomeUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is HomeUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is HomeUiEvent.OnItemRemoved -> onItemRemoved(event.word)
            is HomeUiEvent.OnTextRecognition -> onTextRecognition(event.imagePaths)
            HomeUiEvent.OnItemRestored -> onItemRestored()
            HomeUiEvent.OnSwitchLanguages -> switchLanguageOptions(
                origin = uiState.value.selectedLanguageOptions.first,
                target = uiState.value.selectedLanguageOptions.second
            )

            HomeUiEvent.OnShowcaseCompleted -> onShowcaseCompleted()
            HomeUiEvent.OnReload -> reload()
            HomeUiEvent.OnShowIntroShowcaseIfNecessary -> showIntroShowcaseIfNecessary()
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        _effect.trySend(effect)
    }

    private fun onShowcaseCompleted() {
        viewModelScope.launch {
            // Perform "click" to continue show case intro
            onIconStateChange(!uiState.value.fabState)
            // after fab's content is hidden again, save flag to preferences datastore
            if (!uiState.value.fabState) {
                userPrefsRepository.setFeatureVisited(UserPrefsRepositoryImpl.HOME_FEATURE)
            }
        }
    }

    private fun onShowLanguageDialog(option: TranslationOption?) {
        _uiState.update { it.copy(showLanguageDialog = option) }
    }

    private fun onIconStateChange(boolean: Boolean) {
        _uiState.update { it.copy(fabState = boolean) }
    }

    private fun onItemRemoved(word: WordsAndSentences) {
        addDeletedItemToStack(word)
        deleteWord(word.id)
        _uiState.update { it.copy(showUndoButton = true) }
    }

    private fun onItemRestored() {
        insertWord(deletedItemsStack.last())
        removeDeletedItemFromStack()
        _uiState.update { it.copy(showUndoButton = deletedItemsStack.isNotEmpty()) }
    }

    private fun onTextRecognition(uris: List<Uri>) {
        _uiState.update {
            it.copy(showProgressBar = true)
        }

        viewModelScope.launch {
            runTextRecognition(uris).firstOrNull()?.let { result ->
                _uiState.update { state ->
                    if (result.isEmpty()) sendEffect(HomeUiEffect.ImageTextless)
                    state.copy(showProgressBar = false)
                }
                if (result.isNotEmpty()) {
                    _textResultChannel.trySend(result)
                }
            }
        }
    }

    private fun reload() {
        userRepository.reloadUserData()?.run {
            _uiState.update {
                it.copy(profilePictureUrl = profilePictureUrl)
            }
        }
    }

    private fun showIntroShowcaseIfNecessary() {
        viewModelScope.launch {
            userPrefsRepository.isFeatureVisited(UserPrefsRepositoryImpl.HOME_FEATURE).firstOrNull()
                ?.let { isVisited ->
                    _uiState.update { it.copy(showIntroShowcase = !isVisited) }
                }
        }
    }

    private fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> =
        textRecognitionRepository.runTextRecognition(imagePaths)

    private fun onConfirmLanguageDialog(language: TranslationOption) {
        if (language is TranslationOption.Origin) {
            saveTranslationOptions(Pair(first = language, second = null))
        } else {
            saveTranslationOptions(
                Pair(
                    first = null,
                    second = language as TranslationOption.Target
                )
            )
        }
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