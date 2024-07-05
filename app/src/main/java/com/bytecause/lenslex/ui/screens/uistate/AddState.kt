package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption


data class AddState(
    val textValue: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val isLoading: Boolean = false,
    val showNetworkErrorDialog: Boolean = false,
    val showNetworkErrorMessage: Boolean = false,
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val shouldNavigateBack: Boolean = false
)
