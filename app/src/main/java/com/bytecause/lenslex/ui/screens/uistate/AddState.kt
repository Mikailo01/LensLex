package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Immutable
data class AddState(
    val textValue: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val shouldNavigateBack: Boolean = false
)
