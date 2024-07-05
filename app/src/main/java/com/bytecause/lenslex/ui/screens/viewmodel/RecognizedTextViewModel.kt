package com.bytecause.lenslex.ui.screens.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.TextLanguageRecognitionRepository
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.RecognizedTextUiEvent
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.screens.uistate.RecognizedTextState
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.bytecause.lenslex.util.NetworkUtil
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class RecognizedTextViewModel(
    private val wordsRepository: WordsRepository,
    private val translateRepository: TranslateRepository,
    private val languageRecognitionRepository: TextLanguageRecognitionRepository,
    translationModelManager: TranslationModelManager,
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepository, translationModelManager, supportedLanguagesRepository) {

    private val _uiState = MutableStateFlow(RecognizedTextState())
    val uiState: StateFlow<RecognizedTextState> = _uiState.asStateFlow()

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

    fun uiEventHandler(event: RecognizedTextUiEvent.NonDirect) {
        when (event) {
            RecognizedTextUiEvent.OnFabActionButtonClick -> translateAllText()
            RecognizedTextUiEvent.OnHintActionIconClick -> {}
            RecognizedTextUiEvent.OnSelectAllWords -> onSelectAllWordsHandler()
            RecognizedTextUiEvent.OnSentenceCancelled -> onSentenceCancelledHandler()
            RecognizedTextUiEvent.OnSentenceDone -> onSentenceDoneHandler()
            RecognizedTextUiEvent.OnUnselectAllWords -> onUnselectAllWordsHandler()
            RecognizedTextUiEvent.OnDismissNetworkErrorDialog -> onDismissNetworkErrorDialogHandler()
            RecognizedTextUiEvent.OnTryAgainClick -> onTryAgainClickHandler()
            is RecognizedTextUiEvent.OnWordClick -> onWordClickHandler(event.word)
            is RecognizedTextUiEvent.OnWordLongClick -> onWordLongClickHandler(event.word)
            is RecognizedTextUiEvent.OnShowLanguageDialog -> onShowLanguageDialogHandler(event.value)
            is RecognizedTextUiEvent.OnConfirmDialog -> onConfirmDialogHandler(event.lang)
            is RecognizedTextUiEvent.OnDownloadLanguage -> downloadModel(event.langCode)
            is RecognizedTextUiEvent.OnRemoveLanguage -> removeModel(event.langCode)
            is RecognizedTextUiEvent.OnAddWords -> onAddWordsHandler(event.words)
        }
    }

    private fun onTryAgainClickHandler() {
        viewModelScope.launch {
            if (NetworkUtil.isOnline()) {
                _uiState.update {
                    it.copy(
                        showNetworkErrorDialog = false,
                        showNetworkErrorMessage = false
                    )
                }
                translateAllText()
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

    private fun onWordClickHandler(word: Word) {
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

    private fun onWordLongClickHandler(word: Word) {
        _uiState.update {
            it.copy(isSentence = true, sentence = it.sentence + word)
        }
    }

    private fun onSentenceDoneHandler() {
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

    private fun onSentenceCancelledHandler() {
        _uiState.update {
            it.copy(isSentence = false, sentence = emptyList())
        }
    }

    private fun onUnselectAllWordsHandler() {
        _uiState.update {
            // Remove all selected single words and leave sentences only
            it.copy(selectedWords = it.selectedWords - it.words.toSet())
        }
    }

    private fun onSelectAllWordsHandler() {
        _uiState.update {
            it.copy(selectedWords = it.selectedWords + it.words.toSet())
        }
    }

    private fun onShowLanguageDialogHandler(option: TranslationOption?) {
        if (option is TranslationOption.Target || option == null) {
            _uiState.update {
                it.copy(showLanguageDialog = option)
            }
        }
    }

    private fun onConfirmDialogHandler(lang: TranslationOption) {
        _uiState.update {
            it.copy(
                showLanguageDialog = null,
                selectedLanguageOptions = it.selectedLanguageOptions.copy(second = lang as TranslationOption.Target)
            )
        }
    }

    private fun onAddWordsHandler(words: List<Word>) {
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
                                if (index == textSet.size - 1) _uiState.update {
                                    it.copy(
                                        shouldNavigateBack = true
                                    )
                                }
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