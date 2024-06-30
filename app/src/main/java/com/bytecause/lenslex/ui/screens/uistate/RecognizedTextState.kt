package com.bytecause.lenslex.ui.screens.uistate

import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.interfaces.TranslationOption

data class RecognizedTextState(
    val isSentence: Boolean = false,
    val sentence: List<Word> = emptyList(),
    val words: List<Word> = emptyList(),
    val selectedWords: Set<Word> = emptySet(),
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val isLoading: Boolean = false,
    val showLanguageDialog: TranslationOption? = null,
    val shouldNavigateBack: Boolean = false
)