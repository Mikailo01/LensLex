package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.screens.uistate.AddState
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import kotlinx.coroutines.Job
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
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepository, supportedLanguagesRepository) {

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
            is AddUiEvent.OnTextValueChange -> {
                _uiState.update { it.copy(textValue = event.text) }
            }

            is AddUiEvent.OnConfirmDialog -> {
                saveTranslationOption(event.value)
                _uiState.update { it.copy(showLanguageDialog = null) }
            }

            is AddUiEvent.OnDownloadLanguage -> {
                downloadModel(event.langCode)
            }

            is AddUiEvent.OnRemoveLanguage -> {
                removeModel(event.langCode)
            }

            is AddUiEvent.OnShowLanguageDialog -> {
                _uiState.update { it.copy(showLanguageDialog = event.value) }
            }

            is AddUiEvent.OnTranslate -> translateText(event.value)

            AddUiEvent.OnNavigateBack -> {
                _uiState.update { it.copy(shouldNavigateBack = true) }
            }
        }
    }

    private fun translateText(text: String) {
        if (translateJob?.isActive == true) return

        val sourceLang = uiState.value.selectedLanguageOptions.first.lang.langCode

        translateJob = viewModelScope.launch {
            translateRepository.translate(
                text = text,
                sourceLang = sourceLang,
                targetLang = uiState.value.selectedLanguageOptions.second.lang.langCode
            ).firstOrNull()?.let { translationResult ->
                when (translationResult) {
                    Translator.TranslationResult.ModelDownloadFailure -> {
                        /* Toast.makeText(
                             context,
                             "Model download failed.",
                             Toast.LENGTH_SHORT
                         ).show()*/
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
                        /*Toast.makeText(
                            context,
                            "Translation failed.",
                            Toast.LENGTH_SHORT
                        )
                            .show()*/
                    }
                }
            }
        }
    }

    private fun insertWord(word: WordsAndSentences): Flow<Boolean> =
        wordsRepository.addWord(word)
}