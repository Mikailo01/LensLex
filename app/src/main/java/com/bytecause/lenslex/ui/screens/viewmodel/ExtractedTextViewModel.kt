package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.abstraction.TextLanguageRecognitionRepository
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.ExtractedTextUiEffect
import com.bytecause.lenslex.ui.events.ExtractedTextUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.screens.uistate.RecognizedTextState
import com.bytecause.lenslex.util.NetworkUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ExtractedTextViewModel(
    private val wordsRepository: WordsRepository,
    private val translateRepository: TranslateRepository,
    private val languageRecognitionRepository: TextLanguageRecognitionRepository,
    private val userPrefsRepository: UserPrefsRepository,
    translationModelManager: TranslationModelManager,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : TranslationViewModel(
    userPrefsRepository,
    translationModelManager,
    supportedLanguagesRepository
) {

    private val _uiState = MutableStateFlow(RecognizedTextState())
    val uiState: StateFlow<RecognizedTextState> = _uiState.asStateFlow()

    private val _effect = Channel<ExtractedTextUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    init {
        combine(
            languageOptionFlow,
            supportedLanguages
        ) { selectedLang, supportedLanguages ->

            if (uiState.value.selectedLanguageOptions.second.lang.langCode.isBlank()) {
                _uiState.update {
                    it.copy(selectedLanguageOptions = it.selectedLanguageOptions.copy(second = selectedLang.second))
                }
            }

            if (uiState.value.supportedLanguages != supportedLanguages) _uiState.update {
                it.copy(
                    supportedLanguages = supportedLanguages
                )
            }
        }.launchIn(viewModelScope)
    }

    fun uiEventHandler(event: ExtractedTextUiEvent.NonDirect) {
        when (event) {
            ExtractedTextUiEvent.OnFabActionButtonClick -> translateAllText()
            ExtractedTextUiEvent.OnHintActionIconClick -> onHintActionIconClick()
            ExtractedTextUiEvent.OnSelectAllWords -> onSelectAllWords()
            ExtractedTextUiEvent.OnSentenceCancelled -> onSentenceCancelled()
            ExtractedTextUiEvent.OnSentenceDone -> onSentenceDone()
            ExtractedTextUiEvent.OnUnselectAllWords -> onUnselectAllWords()
            ExtractedTextUiEvent.OnDismissNetworkErrorDialog -> onDismissNetworkErrorDialog()
            ExtractedTextUiEvent.OnTryAgainClick -> onTryAgainClick()
            ExtractedTextUiEvent.OnShowcaseCompleted -> onShowcaseCompletedHandler()
            ExtractedTextUiEvent.OnShowIntroShowcaseIfNecessary -> showIntroShowcaseIfNecessary()
            is ExtractedTextUiEvent.OnWordClick -> onWordClick(event.word)
            is ExtractedTextUiEvent.OnWordLongClick -> onWordLongClick(event.word)
            is ExtractedTextUiEvent.OnShowLanguageDialog -> onShowLanguageDialog(event.value)
            is ExtractedTextUiEvent.OnConfirmLanguageDialog -> onConfirmLanguageDialog(event.lang)
            is ExtractedTextUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is ExtractedTextUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is ExtractedTextUiEvent.OnAddWords -> onAddWords(event.words)
        }
    }

    private fun sendEffect(effect: ExtractedTextUiEffect) {
        _effect.trySend(effect)
    }

    private fun onHintActionIconClick() {
        _uiState.update { it.copy(showIntroShowcase = true) }
        sendEffect(ExtractedTextUiEffect.ResetIntroShowcaseState)
    }

    private fun onShowcaseCompletedHandler() {
        // TODO("Fix")
        viewModelScope.launch {
            if (userPrefsRepository.isFeatureVisited(UserPrefsRepositoryImpl.EXTRACTED_TEXT_FEATURE)
                    .firstOrNull() == true
            ) {
                onUnselectAllWords()
                return@launch
            }

            if (!uiState.value.isSentence) {
                onWordLongClick(uiState.value.words.first())
            } else if (uiState.value.selectedWords.isEmpty()) {
                onSentenceCancelled()
                onWordClick(uiState.value.words.first())
                userPrefsRepository.setFeatureVisited(UserPrefsRepositoryImpl.EXTRACTED_TEXT_FEATURE)
            }
        }
    }

    private fun showIntroShowcaseIfNecessary() {
        viewModelScope.launch {
            userPrefsRepository.isFeatureVisited(UserPrefsRepositoryImpl.EXTRACTED_TEXT_FEATURE)
                .firstOrNull()
                ?.let { isVisited ->
                    _uiState.update { it.copy(showIntroShowcase = !isVisited) }
                }
        }
    }

    private fun onTryAgainClick() {
        viewModelScope.launch {
            if (NetworkUtil.isOnline()) {
                _uiState.update {
                    it.copy(
                        showNetworkErrorDialog = false
                    )
                }
                translateAllText()
            } else sendEffect(ExtractedTextUiEffect.ShowNetworkErrorMessage)
        }
    }

    private fun onDismissNetworkErrorDialog() {
        _uiState.update {
            it.copy(showNetworkErrorDialog = false)
        }
    }

    private fun onWordClick(word: Word) {
        _uiState.update { state ->
            if (!uiState.value.isSentence) {
                state.copy(
                    selectedWords = if (state.selectedWords.any { it.text == word.text }) {
                        state.selectedWords.find { it.text == word.text }?.let {
                            state.selectedWords - it
                        } ?: return
                    } else {
                        state.selectedWords + word
                    }
                )
            } else {
                state.copy(
                    sentence = if (state.sentence.contains(word)) {
                        state.sentence - word
                    } else {
                        state.sentence + word
                    }
                )
            }
        }
    }

    private fun onWordLongClick(word: Word) {
        _uiState.update {
            it.copy(isSentence = true, sentence = it.sentence + word)
        }
    }

    private fun onSentenceDone() {
        val constructedSentence = uiState.value.sentence.joinToString(" ") { it.text }

        _uiState.update {
            it.copy(isSentence = false, sentence = emptyList())
        }

        if (uiState.value.selectedWords.none { it.text == constructedSentence }) {
            _uiState.update {
                it.copy(
                    selectedWords = it.selectedWords + Word(
                        id = it.words.last().id + 1,
                        text = constructedSentence
                    )
                )
            }
        }
    }

    private fun onSentenceCancelled() {
        _uiState.update {
            it.copy(isSentence = false, sentence = emptyList())
        }
    }

    private fun onUnselectAllWords() {
        _uiState.update {
            // Remove all selected single words and leave sentences only
            it.copy(selectedWords = it.selectedWords - it.words.toSet())
        }
    }

    private fun onSelectAllWords() {
        _uiState.update {
            it.copy(selectedWords = it.selectedWords + it.words.toSet())
        }
    }

    private fun onShowLanguageDialog(option: TranslationOption?) {
        if (option is TranslationOption.Target || option == null) {
            _uiState.update {
                it.copy(showLanguageDialog = option)
            }
        } else sendEffect(ExtractedTextUiEffect.ShowLanguageOptionMessage)
    }

    private fun onConfirmLanguageDialog(translationOption: TranslationOption) {
        if ((translationOption as TranslationOption.Target).lang.langCode != uiState.value.selectedLanguageOptions.first.lang.langCode) {
            _uiState.update {
                it.copy(
                    showLanguageDialog = null,
                    selectedLanguageOptions = it.selectedLanguageOptions.copy(second = translationOption)
                )
            }
        } else sendEffect(ExtractedTextUiEffect.ShowChooseDifferentLanguageOptionMessage)

    }

    private fun onAddWords(words: List<Word>) {
        runLangRecognition(words)
        addWords(words)
    }

    private fun runLangRecognition(words: List<Word>) {
        viewModelScope.launch {
            languageRecognitionRepository.runLangRecognition(words.joinToString(" ") { it.text })
                .firstOrNull()?.let { langCode ->
                    if (langCode != "und") {
                        _uiState.update {
                            it.copy(
                                selectedLanguageOptions = it.selectedLanguageOptions.copy(
                                    first = TranslationOption.Origin(
                                        lang = SupportedLanguage(
                                            langCode = langCode,
                                            langName = Locale(langCode).displayName
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun translateAllText() {
        if (uiState.value.isLoading) return
        if (uiState.value.selectedLanguageOptions.second.lang.langCode.isBlank()) {
            sendEffect(ExtractedTextUiEffect.ShowMissingLanguageOptionMessage)
            return
        }

        viewModelScope.launch {
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

            val textSet = uiState.value.selectedWords
            val sourceLang = uiState.value.selectedLanguageOptions.first.lang.langCode

            textSet.forEachIndexed { index, text ->
                translateRepository.translate(
                    text = text.text,
                    sourceLang = sourceLang,
                    targetLang = uiState.value.selectedLanguageOptions.second.lang.langCode
                ).firstOrNull()?.let { result ->
                    when (result) {
                        Translator.TranslationResult.ModelDownloadFailure -> {
                            _uiState.update {
                                it.copy(isLoading = false)
                            }
                        }

                        is Translator.TranslationResult.TranslationSuccess -> {
                            insertWord(
                                WordsAndSentences(
                                    id = "${text.text}_${sourceLang}"
                                        .replace(" ", "_"),
                                    word = text.text,
                                    languageCode = uiState.value.selectedLanguageOptions.first.lang.langCode,
                                    translations = mapOf(uiState.value.selectedLanguageOptions.second.lang.langCode to result.translatedText),
                                    timeStamp = System.currentTimeMillis()
                                )
                            ).firstOrNull().let {
                                if (index == textSet.size - 1) sendEffect(ExtractedTextUiEffect.NavigateBack)
                            }
                        }

                        Translator.TranslationResult.TranslationFailure -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            }
        }
    }

    private fun insertWord(word: WordsAndSentences): Flow<Boolean> =
        wordsRepository.addWord(word)

    private fun addWords(words: List<Word>) {
        _uiState.update { it.copy(words = words) }
    }
}