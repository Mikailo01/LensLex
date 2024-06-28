package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.domain.models.Word
import com.bytecause.lenslex.ui.interfaces.TranslationOption

sealed interface RecognizedTextUiEvent {

    sealed interface Direct : RecognizedTextUiEvent
    sealed interface NonDirect : RecognizedTextUiEvent

    data class OnWordClick(val word: Word) : NonDirect
    data class OnWordLongClick(val word: Word) : NonDirect
    data class OnShowLanguageDialog(val value: TranslationOption?) : NonDirect
    data class OnConfirmDialog(val lang: TranslationOption) : NonDirect
    data class OnDownloadLanguage(val langCode: String) : NonDirect
    data class OnRemoveLanguage(val langCode: String) : NonDirect
    data class OnAddWords(val words: List<Word>) : NonDirect
    data object OnSentenceDone : NonDirect
    data object OnSentenceCancelled : NonDirect
    data object OnSelectAllWords : NonDirect
    data object OnUnselectAllWords : NonDirect
    data object OnHintActionIconClick : NonDirect
    data object OnFabActionButtonClick : NonDirect

    data object OnBackButtonClick : Direct
    data object OnStartShareIntent : Direct
    data object OnCopyContent : Direct

}