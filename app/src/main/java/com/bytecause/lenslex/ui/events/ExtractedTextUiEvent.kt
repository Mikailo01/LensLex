package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.interfaces.TranslationOption

sealed interface ExtractedTextUiEvent {

    sealed interface Direct : ExtractedTextUiEvent
    sealed interface NonDirect : ExtractedTextUiEvent

    data class OnWordClick(val word: Word) : NonDirect
    data class OnWordLongClick(val word: Word) : NonDirect
    data class OnShowLanguageDialog(val value: TranslationOption?) : NonDirect
    data class OnConfirmLanguageDialog(val lang: TranslationOption) : NonDirect
    data class OnDownloadLanguage(val langCode: String) : NonDirect
    data class OnRemoveLanguage(val langCode: String) : NonDirect
    data class OnAddWords(val words: List<Word>) : NonDirect
    data object OnSentenceDone : NonDirect
    data object OnSentenceCancelled : NonDirect
    data object OnSelectAllWords : NonDirect
    data object OnUnselectAllWords : NonDirect
    data object OnHintActionIconClick : NonDirect
    data object OnFabActionButtonClick : NonDirect
    data object OnDismissNetworkErrorDialog : NonDirect
    data object OnDismissLanguageInferenceErrorDialog : NonDirect
    data object OnTryAgainClick : NonDirect
    data object OnShowcaseCompleted : NonDirect
    data object OnShowIntroShowcaseIfNecessary : NonDirect
    data object OnSwitchLanguageOptions : NonDirect

    data object OnBackButtonClick : Direct
    data object OnCopyContent : Direct
}

sealed interface ExtractedTextUiEffect {
    data object ShowNetworkErrorMessage : ExtractedTextUiEffect
    data object ShowMissingLanguageOptionMessage : ExtractedTextUiEffect
    data object ResetIntroShowcaseState : ExtractedTextUiEffect
    data object NavigateBack : ExtractedTextUiEffect
}