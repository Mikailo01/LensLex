package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.screens.uistate.AddState
import com.bytecause.lenslex.util.NetworkUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AddViewModel(
    private val wordsRepository: WordsRepository,
    private val translateRepository: TranslateRepository,
    translationModelManager: TranslationModelManager,
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : TranslationViewModel(
    userPrefsRepository,
    translationModelManager,
    supportedLanguagesRepository
) {

    private val _uiState = MutableStateFlow(AddState())
    val uiState = _uiState.asStateFlow()

    private var translateJob: Job? = null

    init {
        combine(
            languageOptionFlow,
            supportedLanguages
        ) { selectedLang, supportedLanguages ->

            if (_uiState.value.selectedLanguageOptions != selectedLang) _uiState.update {
                it.copy(
                    selectedLanguageOptions = selectedLang
                )
            }
            if (_uiState.value.supportedLanguages != supportedLanguages) _uiState.update {
                it.copy(
                    supportedLanguages = supportedLanguages
                )
            }

        }.launchIn(viewModelScope)
    }

    fun uiEventHandler(event: AddUiEvent) {
        when (event) {
            is AddUiEvent.OnTextValueChange -> onTextValueChangeHandler(event.text)
            is AddUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialog(event.value)
            is AddUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is AddUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is AddUiEvent.OnShowLanguageDialog -> onShowLanguageDialogHandler(event.value)
            is AddUiEvent.OnTranslate -> translateText(event.value)
            is AddUiEvent.OnTryAgainClick -> onTryAgainClickHandler(event.value)
            AddUiEvent.OnNavigateBack -> onNavigateBackHandler()
            AddUiEvent.OnDismissNetworkErrorDialog -> onDismissNetworkErrorDialogHandler()
            AddUiEvent.OnSwitchLanguages -> switchLanguageOptions(
                origin = uiState.value.selectedLanguageOptions.first,
                target = uiState.value.selectedLanguageOptions.second
            )
        }
    }

    private fun onTryAgainClickHandler(text: String) {
        viewModelScope.launch {
            if (NetworkUtil.isOnline()) {
                _uiState.update {
                    it.copy(
                        showNetworkErrorDialog = false,
                        showNetworkErrorMessage = false
                    )
                }
                translateText(text)
            } else {
                if (uiState.value.showNetworkErrorMessage) {
                    _uiState.update { it.copy(showNetworkErrorMessage = false) }
                    delay(300)
                    _uiState.update { it.copy(showNetworkErrorMessage = true) }
                } else _uiState.update { it.copy(showNetworkErrorMessage = true) }
            }
        }
    }

    private fun onDismissNetworkErrorDialogHandler() {
        _uiState.update {
            it.copy(showNetworkErrorDialog = false)
        }
    }

    private fun onTextValueChangeHandler(text: String) {
        _uiState.update { it.copy(textValue = text) }
    }

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

    private fun onShowLanguageDialogHandler(translationOption: TranslationOption?) {
        _uiState.update { it.copy(showLanguageDialog = translationOption) }
    }

    private fun onNavigateBackHandler() {
        _uiState.update { it.copy(shouldNavigateBack = true) }
    }

    private fun translateText(text: String) {
        if (translateJob?.isActive == true) return

        uiState.value.textValue

        translateJob = viewModelScope.launch {
            // if language models are not downloaded and the user is offline, show network error dialog
            if (!areModelsReady(
                    origin = uiState.value.selectedLanguageOptions.first,
                    target = uiState.value.selectedLanguageOptions.second
                ) && !NetworkUtil.isOnline()
            ) {
                _uiState.update { it.copy(showNetworkErrorDialog = true) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            val sourceLang = uiState.value.selectedLanguageOptions.first.lang.langCode

            translateRepository.translate(
                text = text,
                sourceLang = sourceLang,
                targetLang = uiState.value.selectedLanguageOptions.second.lang.langCode
            ).firstOrNull()?.let { translationResult ->
                when (translationResult) {
                    Translator.TranslationResult.ModelDownloadFailure -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }

                    is Translator.TranslationResult.TranslationSuccess -> {
                        insertWord(
                            WordsAndSentences(
                                id = "${uiState.value.textValue}_$sourceLang".lowercase()
                                    .replace(" ", "_"),
                                word = uiState.value.textValue,
                                languageCode = uiState.value.selectedLanguageOptions.first.lang.langCode,
                                translations = mapOf(uiState.value.selectedLanguageOptions.second.lang.langCode to translationResult.translatedText),
                                timeStamp = System.currentTimeMillis()
                            )
                        ).firstOrNull().takeIf { it == true }?.let {
                            _uiState.update { it.copy(shouldNavigateBack = true) }
                        }
                    }

                    Translator.TranslationResult.TranslationFailure -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    private fun insertWord(word: WordsAndSentences): Flow<Boolean> =
        wordsRepository.addWord(word)
}