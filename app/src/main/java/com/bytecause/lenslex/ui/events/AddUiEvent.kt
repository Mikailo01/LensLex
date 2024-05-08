package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.models.SupportedLanguage

sealed interface AddUiEvent {

    data class OnTextValueChange(val text: String) : AddUiEvent
    data class OnInsertWord(val translatedText: String) : AddUiEvent
    data class OnConfirmDialog(val value: SupportedLanguage) : AddUiEvent
    data class OnDownloadLanguage(val langCode: String) : AddUiEvent
    data class OnRemoveLanguage(val langCode: String) : AddUiEvent
    data class OnShowLanguageDialog(val value: Boolean) : AddUiEvent
    data object OnDismissDialog : AddUiEvent
    data object OnNavigateBack : AddUiEvent
}