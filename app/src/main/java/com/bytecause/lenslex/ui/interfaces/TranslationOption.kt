package com.bytecause.lenslex.ui.interfaces

import com.bytecause.lenslex.domain.models.SupportedLanguage

sealed interface TranslationOption {
    data class Origin(val lang: SupportedLanguage = SupportedLanguage()) : TranslationOption
    data class Target(val lang: SupportedLanguage = SupportedLanguage()) : TranslationOption
}