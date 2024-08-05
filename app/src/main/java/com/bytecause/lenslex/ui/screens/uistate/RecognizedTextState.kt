package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.ui.models.Word

@Immutable
data class RecognizedTextState(
    val showIntroShowcase: Boolean = false,
    val isSentence: Boolean = false,
    val sentence: List<Word> = emptyList(),
    val words: List<Word> = emptyList(),
    val selectedWords: Set<Word> = emptySet(),
    val languageFilterText: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val isLoading: Boolean = false,
    val showLanguageDialog: TranslationOption? = null,
    val showNetworkErrorDialog: Boolean = false,
    val showLanguageInferenceErrorDialog: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)