package com.bytecause.lenslex.ui.events

import android.net.Uri
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.navigation.Screen

// I defined Direct and NonDirect sealed interfaces to get rid of else statement in when expressions and make
// it clear, where should be each event be handled (Direct = directly inside composable, NonDirect = inside viewmodel)
sealed interface HomeUiEvent {

    sealed interface Direct : HomeUiEvent
    sealed interface NonDirect : HomeUiEvent

    data class OnCameraIntentLaunch(val uri: Uri) : Direct
    data class OnNavigate(val destination: Screen) : Direct
    data object OnMultiplePhotoPickerLaunch : Direct
    data object OnScrollToTop : Direct

    data class OnIconStateChange(val value: Boolean) : NonDirect
    data class OnConfirmLanguageDialog(val value: SupportedLanguage) : NonDirect
    data class OnShowLanguageDialog(val value: Boolean) : NonDirect
    data class OnDownloadLanguage(val langCode: String) : NonDirect
    data class OnRemoveLanguage(val langCode: String) : NonDirect
    data class OnItemRemoved(val word: WordsAndSentences) : NonDirect
    data object OnItemRestored : NonDirect
}