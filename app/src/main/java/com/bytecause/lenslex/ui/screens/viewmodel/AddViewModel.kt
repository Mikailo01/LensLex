package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.models.uistate.AddState
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AddViewModel(
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val firebase: FirebaseFirestore,
    private val auth: Authenticator,
    userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

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
        auth.getAuth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                firebase
                    .collection("users")
                    .document(userId)
                    .collection("WordsAndSentences")
                    .add(word)
            }.invokeOnCompletion { onSuccess() }
        }
    }
}