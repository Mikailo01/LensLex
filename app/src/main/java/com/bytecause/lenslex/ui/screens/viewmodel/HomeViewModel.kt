package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.TranslationOptionsDataSource
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.Words
import com.bytecause.lenslex.ui.events.HomeUiEffect
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.screens.uistate.HomeState
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
    private val userRepository: UserRepository,
    private val userPrefsRepository: UserPrefsRepository,
    translationOptionsDataSource: TranslationOptionsDataSource,
    translationModelManager: TranslationModelManager,
    supportedLanguagesRepository: SupportedLanguagesRepository,
) : TranslationViewModel(
    userPrefsRepository,
    translationModelManager,
    translationOptionsDataSource,
    supportedLanguagesRepository
) {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    init {
        combine(
            languageOptionFlow,
            supportedLanguages,
        ) { selectedLang, supportedLanguages ->

            _uiState.update { state ->
                state.copy(
                    selectedLanguageOptions = selectedLang.takeIf { it != state.selectedLanguageOptions }
                        ?: state.selectedLanguageOptions,
                    supportedLanguages = supportedLanguages.takeIf { it != state.supportedLanguages }
                        ?: state.supportedLanguages,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun uiEventHandler(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnIconStateChange -> onIconStateChange(event.value)
            is HomeUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialog(event.value)
            is HomeUiEvent.OnShowLanguageDialog -> onShowLanguageDialog(event.value)
            is HomeUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is HomeUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is HomeUiEvent.OnItemRemoved -> onItemRemoved(event.word)
            is HomeUiEvent.OnTextRecognition -> onTextRecognition(event.imagePaths)
            is HomeUiEvent.OnEditStateChange -> onEditStateChange(event.value)
            is HomeUiEvent.OnDeleteConfirmationDialogResult -> onDeleteConfirmationDialogResult(
                event.value
            )

            is HomeUiEvent.OnNavigate -> sendEffect(HomeUiEffect.NavigateTo(event.destination))
            is HomeUiEvent.OnSpeak -> sendEffect(HomeUiEffect.Speak(event.text, event.langCode))
            is HomeUiEvent.OnLanguageFilterTextChange -> onFilterTextChange(event.text)

            HomeUiEvent.OnItemRestored -> onItemRestored()
            HomeUiEvent.OnSwitchLanguages -> switchLanguageOptions(
                origin = uiState.value.selectedLanguageOptions.first,
                target = uiState.value.selectedLanguageOptions.second
            )

            HomeUiEvent.OnShowcaseCompleted -> onShowcaseCompleted()
            HomeUiEvent.OnReload -> reload()
            HomeUiEvent.OnShowIntroShowcaseIfNecessary -> showIntroShowcaseIfNecessary()
            HomeUiEvent.OnFetchItemList -> onFetchItemList()
            HomeUiEvent.OnCameraIntentLaunch -> sendEffect(HomeUiEffect.CameraIntentLaunch)
            HomeUiEvent.OnMultiplePhotoPickerLaunch -> sendEffect(HomeUiEffect.MultiplePhotoPickerLaunch)
            HomeUiEvent.OnPermissionDialogLaunch -> sendEffect(HomeUiEffect.PermissionDialogLaunch)
            HomeUiEvent.OnScrollToTop -> sendEffect(HomeUiEffect.ScrollToTop)
            HomeUiEvent.OnUpdateSupportedLanguages -> updateSupportedLanguages()
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        _effect.trySend(effect)
    }

    private fun onShowcaseCompleted() {
        viewModelScope.launch {
            // reset to initial state
            _uiState.update {
                it.copy(
                    wordList = emptyList(),
                    fabState = false,
                    deletedItemsStack = emptyList(),
                    showIntroShowcase = false
                )
            }
            // save flag to preferences datastore
            userPrefsRepository.setFeatureVisited(UserPrefsRepositoryImpl.HOME_FEATURE)
        }
    }

    private fun onFilterTextChange(text: String) {
        _uiState.update {
            it.copy(
                languageFilterText = text,
                supportedLanguages = if (text.isBlank()) supportedLanguages.value else supportedLanguages.value.filter { lang ->
                    lang.langName.startsWith(
                        text,
                        ignoreCase = true
                    )
                }
            )
        }
    }

    private fun onDeleteConfirmationDialogResult(boolean: Boolean) {
        if (boolean) {
            _uiState.update {
                it.copy(
                    deletedItemsStack = emptyList(),
                    isEditEnabled = false,
                    showDeleteConfirmationDialog = false
                )
            }
        } else _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    private fun onFetchItemList() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            uiState.value.selectedLanguageOptions.run {
                wordsRepository.getWords(
                    originLangCode = first.lang.langCode,
                    targetLangCode = second.lang.langCode
                ).firstOrNull()?.let { words ->
                    _uiState.update { state ->
                        state.copy(wordList = words, isLoading = false)
                    }
                }
            }
        }
    }

    private fun onEditStateChange(boolean: Boolean) {
        if (!boolean && uiState.value.deletedItemsStack.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
        } else {
            _uiState.update {
                it.copy(
                    isEditEnabled = boolean
                )
            }
        }
    }

    private fun onShowLanguageDialog(option: TranslationOption?) {
        _uiState.update { it.copy(showLanguageDialog = option, languageFilterText = "") }
    }

    private fun onIconStateChange(boolean: Boolean) {
        _uiState.update { it.copy(fabState = boolean) }
    }

    private fun onItemRemoved(word: Words) {
        addDeletedItemToStack(word)
        deleteWord(word.id)
    }

    private fun onItemRestored() {
        insertWord(uiState.value.deletedItemsStack.last())
        removeDeletedItemFromStack()
    }

    private fun onTextRecognition(uris: List<Uri>) {
        _uiState.update {
            it.copy(showProgressBar = true)
        }

        viewModelScope.launch {
            runTextRecognition(uris).firstOrNull()?.let { result ->
                if (result.isEmpty()) {
                    sendEffect(HomeUiEffect.ImageTextless)
                    _uiState.update { state ->
                        state.copy(showProgressBar = false)
                    }
                } else sendEffect(HomeUiEffect.TextResult(result))
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
                    if (isVisited) return@launch

                    // change states to make visible all needed ui elements
                    val fakeWordList = listOf(
                        Words(
                            id = "0",
                            word = "Intro",
                            languageCode = "",
                            translations = mapOf("" to "Intro")
                        )
                    )

                    _uiState.update {
                        it.copy(
                            wordList = it.wordList.ifEmpty {
                                fakeWordList
                            },
                            fabState = true,
                            deletedItemsStack = fakeWordList,
                            showIntroShowcase = true
                        )
                    }
                }
        }
    }

    private fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> =
        textRecognitionRepository.runTextRecognition(imagePaths)

    private fun onConfirmLanguageDialog(language: TranslationOption) {
        when (language) {
            is TranslationOption.Origin -> {
                saveTranslationOptions(Pair(first = language, second = null))
            }

            is TranslationOption.Target -> {
                saveTranslationOptions(
                    Pair(
                        first = null,
                        second = language
                    )
                )
            }
        }
        _uiState.update { it.copy(showLanguageDialog = null, languageFilterText = "") }
    }

    private fun insertWord(word: Words) {
        viewModelScope.launch {
            wordsRepository.addWord(word).firstOrNull()
        }
    }

    private fun deleteWord(documentId: String) {
        wordsRepository.deleteWord(documentId)
    }

    private fun addDeletedItemToStack(item: Words) {
        _uiState.update {
            it.copy(deletedItemsStack = it.deletedItemsStack + item)
        }
    }

    private fun removeDeletedItemFromStack() {
        _uiState.update {
            it.copy(
                deletedItemsStack = it.deletedItemsStack
                    .toMutableList()
                    .apply {
                        removeLast()
                    }
            )
        }
    }
}