package com.bytecause.lenslex.ui.events

import android.net.Uri
import com.bytecause.lenslex.domain.models.Words
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.interfaces.TranslationOption

sealed interface HomeUiEvent {
    data class OnSpeak(val text: String, val langCode: String) : HomeUiEvent
    data class OnNavigate(val destination: Screen) : HomeUiEvent
    data class OnIconStateChange(val value: Boolean) : HomeUiEvent
    data class OnConfirmLanguageDialog(val value: TranslationOption) : HomeUiEvent
    data class OnShowLanguageDialog(val value: TranslationOption?) : HomeUiEvent
    data class OnDownloadLanguage(val langCode: String) : HomeUiEvent
    data class OnRemoveLanguage(val langCode: String) : HomeUiEvent
    data class OnTextRecognition(val imagePaths: List<Uri>) : HomeUiEvent
    data class OnItemRemoved(val word: Words) : HomeUiEvent
    data class OnEditStateChange(val value: Boolean) : HomeUiEvent
    data class OnDeleteConfirmationDialogResult(val value: Boolean) : HomeUiEvent
    data object OnCameraIntentLaunch : HomeUiEvent
    data object OnMultiplePhotoPickerLaunch : HomeUiEvent
    data object OnScrollToTop : HomeUiEvent
    data object OnPermissionDialogLaunch : HomeUiEvent
    data object OnItemRestored : HomeUiEvent
    data object OnSwitchLanguages : HomeUiEvent
    data object OnShowcaseCompleted : HomeUiEvent
    data object OnReload : HomeUiEvent
    data object OnShowIntroShowcaseIfNecessary : HomeUiEvent
    data object OnFetchItemList : HomeUiEvent
}

sealed interface HomeUiEffect {
    data object ImageTextless : HomeUiEffect
    data object CameraIntentLaunch : HomeUiEffect
    data object ScrollToTop : HomeUiEffect
    data object MultiplePhotoPickerLaunch : HomeUiEffect
    data object PermissionDialogLaunch : HomeUiEffect
    data class TextResult(val text: List<String>) : HomeUiEffect
    data class Speak(val text: String, val langCode: String) : HomeUiEffect
    data class NavigateTo(val destination: Screen) : HomeUiEffect
}