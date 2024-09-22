package com.bytecause.lenslex.ui.screens.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Immutable
data class AddState(
    val textValue: String = "",
    val languageFilterText: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val isLoading: Boolean = false,
    val showNetworkErrorDialog: Boolean = false,
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
