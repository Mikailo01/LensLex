package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.interfaces.TranslationOption

sealed interface AddUiEvent {

    data class OnTextValueChange(val text: String) : AddUiEvent
    data class OnConfirmLanguageDialog(val value: TranslationOption) : AddUiEvent
    data class OnDownloadLanguage(val langCode: String) : AddUiEvent
    data class OnRemoveLanguage(val langCode: String) : AddUiEvent
    data class OnShowLanguageDialog(val value: TranslationOption?) : AddUiEvent
    data class OnTranslate(val value: String) : AddUiEvent
    data class OnTryAgainClick(val value: String) : AddUiEvent
    data object OnNavigateBack : AddUiEvent
    data object OnDismissNetworkErrorDialog : AddUiEvent
    data object OnSwitchLanguages : AddUiEvent
}