package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage

@Immutable
data class AddState(
    val textValue: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguage: SupportedLanguage = SupportedLanguage(),
    val showLanguageDialog: Boolean = false,
    val shouldNavigateBack: Boolean = false
)
