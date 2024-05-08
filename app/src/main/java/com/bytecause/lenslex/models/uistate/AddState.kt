package com.bytecause.lenslex.models.uistate

import com.bytecause.lenslex.models.SupportedLanguage

data class AddState(
    val textValue: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguage: SupportedLanguage = SupportedLanguage(),
    val showLanguageDialog: Boolean = false,
    val shouldNavigateBack: Boolean = false
)
