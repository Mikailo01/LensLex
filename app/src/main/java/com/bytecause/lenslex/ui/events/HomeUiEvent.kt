package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.models.WordsAndSentences

sealed interface HomeUiEvent {

    data class OnIconStateChange(val value: Boolean) : HomeUiEvent
    data class OnConfirmLanguageDialog(val value: SupportedLanguage) : HomeUiEvent
    data class OnShowLanguageDialog(val value: Boolean) : HomeUiEvent
    data class OnDownloadLanguage(val langCode: String) : HomeUiEvent
    data class OnRemoveLanguage(val langCode: String) : HomeUiEvent
    data class OnItemRemoved(val word: WordsAndSentences) : HomeUiEvent
    data object OnItemRestored : HomeUiEvent
}