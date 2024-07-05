package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.interfaces.TranslationOption

data class HomeState(
    val wordList: List<WordsAndSentences> = emptyList(),
    val profilePictureUrl: String = "",
    val isLoading: Boolean = true,
    val fabState: Boolean = false,
    val showProgressBar: Boolean = false,
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val showUndoButton: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val isImageTextless: Boolean = false
)