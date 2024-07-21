package com.bytecause.lenslex.ui.events

import android.net.Uri
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.interfaces.TranslationOption

// I defined Direct and NonDirect sealed interfaces to get rid of else statement in when expressions and make
// it clear, where should be each event be handled (Direct = directly inside composable, NonDirect = inside viewmodel)
sealed interface HomeUiEvent {

    sealed interface Direct : HomeUiEvent
    sealed interface NonDirect : HomeUiEvent

    data class OnSpeak(val text: String, val langCode: String) : Direct
    data class OnNavigate(val destination: Screen) : Direct
    data object OnCameraIntentLaunch : Direct
    data object OnMultiplePhotoPickerLaunch : Direct
    data object OnScrollToTop : Direct
    data object OnPermissionDialogLaunch : Direct

    data class OnIconStateChange(val value: Boolean) : NonDirect
    data class OnConfirmLanguageDialog(val value: TranslationOption) : NonDirect
    data class OnShowLanguageDialog(val value: TranslationOption?) : NonDirect
    data class OnDownloadLanguage(val langCode: String) : NonDirect
    data class OnRemoveLanguage(val langCode: String) : NonDirect
    data class OnTextRecognition(val imagePaths: List<Uri>) : NonDirect
    data class OnItemRemoved(val word: WordsAndSentences) : NonDirect
    data object OnItemRestored : NonDirect
    data object OnSwitchLanguages : NonDirect
    data object OnShowcaseCompleted : NonDirect
    data object OnReload : NonDirect
    data object OnShowIntroShowcaseIfNecessary : NonDirect
}

sealed interface HomeUiEffect {
    data object ImageTextless : HomeUiEffect
}