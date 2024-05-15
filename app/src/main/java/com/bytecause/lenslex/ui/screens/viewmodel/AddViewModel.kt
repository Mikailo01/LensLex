package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.WordsRepositoryImpl
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.screens.uistate.AddState
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AddViewModel(
    private val firestoreRepository: WordsRepositoryImpl,
    userPrefsRepository: UserPrefsRepository,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepository, supportedLanguagesRepository) {

    private val _uiState = MutableStateFlow(AddState())
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            languageOptionFlow,
            supportedLanguages
        ) { selectedLang, supportedLanguages ->

            if (_uiState.value.selectedLanguage != selectedLang) _uiState.update {
                it.copy(
                    selectedLanguage = selectedLang
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

            is AddUiEvent.OnInsertWord -> {
                insertWord(
                    WordsAndSentences(
                        id = "${_uiState.value.textValue}_en".lowercase()
                            .replace(" ", "_"),
                        word = _uiState.value.textValue,
                        languageCode = "en",
                        translations = mapOf(_uiState.value.selectedLanguage.langCode to event.translatedText),
                        timeStamp = System.currentTimeMillis()
                    )
                ) {
                    _uiState.update { it.copy(shouldNavigateBack = true) }
                }
            }

            is AddUiEvent.OnConfirmDialog -> {
                _uiState.update { it.copy(showLanguageDialog = false) }
            }

            is AddUiEvent.OnDownloadLanguage -> {
                downloadModel(event.langCode)
            }

            is AddUiEvent.OnRemoveLanguage -> {
                removeModel(event.langCode)
            }

            is AddUiEvent.OnShowLanguageDialog -> {
                _uiState.update { it.copy(showLanguageDialog = true) }
            }

            AddUiEvent.OnDismissDialog -> {
                _uiState.update { it.copy(showLanguageDialog = false) }
            }

            AddUiEvent.OnNavigateBack -> {
                _uiState.update { it.copy(shouldNavigateBack = true) }
            }
        }
    }

    private fun insertWord(word: WordsAndSentences, onSuccess: () -> Unit) {
        viewModelScope.launch {
            firestoreRepository.addWord(word).firstOrNull()?.let {
                if (it) onSuccess()
            }
        }.invokeOnCompletion {  }
    }
}