package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.interfaces.TranslationOption

sealed interface ExtractedTextUiEvent {
    data class OnWordClick(val word: Word) : ExtractedTextUiEvent
    data class OnWordLongClick(val word: Word) : ExtractedTextUiEvent
    data class OnShowLanguageDialog(val value: TranslationOption?) : ExtractedTextUiEvent
    data class OnConfirmLanguageDialog(val lang: TranslationOption) : ExtractedTextUiEvent
    data class OnDownloadLanguage(val langCode: String) : ExtractedTextUiEvent
    data class OnRemoveLanguage(val langCode: String) : ExtractedTextUiEvent
    data class OnAddWords(val words: List<Word>) : ExtractedTextUiEvent
    data object OnSentenceDone : ExtractedTextUiEvent
    data object OnSentenceCancelled : ExtractedTextUiEvent
    data object OnSelectAllWords : ExtractedTextUiEvent
    data object OnUnselectAllWords : ExtractedTextUiEvent
    data object OnHintActionIconClick : ExtractedTextUiEvent
    data object OnFabActionButtonClick : ExtractedTextUiEvent
    data object OnDismissNetworkErrorDialog : ExtractedTextUiEvent
    data object OnDismissLanguageInferenceErrorDialog : ExtractedTextUiEvent
    data object OnTryAgainClick : ExtractedTextUiEvent
    data object OnShowcaseCompleted : ExtractedTextUiEvent
    data object OnShowIntroShowcaseIfNecessary : ExtractedTextUiEvent
    data object OnSwitchLanguageOptions : ExtractedTextUiEvent
    data object OnBackButtonClick : ExtractedTextUiEvent
    data object OnCopyContent : ExtractedTextUiEvent
}

sealed interface ExtractedTextUiEffect {
    data object ShowNetworkErrorMessage : ExtractedTextUiEffect
    data object ShowMissingLanguageOptionMessage : ExtractedTextUiEffect
    data object ResetIntroShowcaseState : ExtractedTextUiEffect
    data object Done : ExtractedTextUiEffect
    data object NavigateBack : ExtractedTextUiEffect
    data object CopyContent : ExtractedTextUiEffect
}