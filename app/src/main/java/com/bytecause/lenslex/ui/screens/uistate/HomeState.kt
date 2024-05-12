package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences

@Immutable
data class HomeState(
    val wordList: List<WordsAndSentences> = emptyList(),
    val profilePictureUrl: String = "",
    val isLoading: Boolean = true,
    val fabState: Boolean = false,
    val showProgressBar: Boolean = false,
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguage: SupportedLanguage = SupportedLanguage(),
    val showLanguageDialog: Boolean = false,
    val showUndoButton: Boolean = false
)