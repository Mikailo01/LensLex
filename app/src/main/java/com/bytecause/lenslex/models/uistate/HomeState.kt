package com.bytecause.lenslex.models.uistate

import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.models.WordsAndSentences

data class HomeState(
    val wordList: List<WordsAndSentences> = emptyList(),
    val profilePictureUrl: String = "",
    val fabState: Boolean = false,
    val showProgressBar: Boolean = false,
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguage: SupportedLanguage = SupportedLanguage(),
    val showLanguageDialog: Boolean = false,
    val showUndoButton: Boolean = false
)