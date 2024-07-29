package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.TranslationOptionsDataSource
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.AddUiEffect
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.screens.uistate.AddState
import com.bytecause.lenslex.util.NetworkUtil
import kotlinx.coroutines.Job
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

class AddViewModel(
    private val wordsRepository: WordsRepository,
    private val translateRepository: TranslateRepository,
    translationOptionsDataSource: TranslationOptionsDataSource,
    translationModelManager: TranslationModelManager,
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : TranslationViewModel(
    userPrefsRepository,
    translationModelManager,
    translationOptionsDataSource,
    supportedLanguagesRepository
) {

    private val _uiState = MutableStateFlow(AddState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AddUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

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
            is AddUiEvent.OnTextValueChange -> onTextValueChange(event.text)
            is AddUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialog(event.value)
            is AddUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is AddUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is AddUiEvent.OnShowLanguageDialog -> onShowLanguageDialog(event.value)
            is AddUiEvent.OnTranslate -> translateText(event.value)
            is AddUiEvent.OnTryAgainClick -> onTryAgainClick(event.value)
            AddUiEvent.OnNavigateBack -> sendEffect(AddUiEffect.NavigateBack)
            AddUiEvent.OnDismissNetworkErrorDialog -> onDismissNetworkErrorDialog()
            AddUiEvent.OnSwitchLanguages -> switchLanguageOptions(
                origin = uiState.value.selectedLanguageOptions.first,
                target = uiState.value.selectedLanguageOptions.second
            )
        }
    }

    private fun sendEffect(effect: AddUiEffect) {
        _effect.trySend(effect)
    }

    private fun onTryAgainClick(text: String) {
        viewModelScope.launch {
            if (NetworkUtil.isOnline()) {
                _uiState.update {
                    it.copy(
                        showNetworkErrorDialog = false
                    )
                }
                translateText(text)
            } else {
                sendEffect(AddUiEffect.ShowNetworkErrorMessage)
            }
        }
    }

    private fun onDismissNetworkErrorDialog() {
        _uiState.update {
            it.copy(showNetworkErrorDialog = false)
        }
    }

    private fun onTextValueChange(text: String) {
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

    private fun onShowLanguageDialog(translationOption: TranslationOption?) {
        _uiState.update { it.copy(showLanguageDialog = translationOption) }
    }

    private fun translateText(text: String) {
        if (translateJob?.isActive == true) return

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
                            sendEffect(AddUiEffect.NavigateBack)
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