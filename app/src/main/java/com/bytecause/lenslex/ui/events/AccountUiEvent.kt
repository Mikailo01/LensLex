package com.bytecause.lenslex.ui.events

import android.net.Uri
import com.bytecause.lenslex.navigation.Screen

// I defined Direct and NonDirect sealed interfaces to get rid of else statement in when expressions and make
// it clear, where should be each event be handled (Direct = directly inside composable, NonDirect = inside viewmodel)
sealed interface AccountUiEvent {

    sealed interface Direct : AccountUiEvent
    sealed interface NonDirect : AccountUiEvent

    data class OnNavigate(val destination: Screen) : Direct
    data object OnBackButtonClick : Direct
    data object OnSinglePicturePickerLaunch : Direct

    data class OnUpdateProfilePicture(val value: String) : NonDirect
    data class OnChangeFirebaseLanguage(val value: String) : NonDirect
    data class OnShowConfirmationDialog(val value: Boolean) : NonDirect
    data class OnShowLanguageDialog(val value: Boolean) : NonDirect
    data class OnShowBottomSheet(val value: Boolean) : NonDirect
    data class OnShowUrlDialog(val value: Boolean) : NonDirect
    data class OnNameTextFieldValueChange(val value: String) : NonDirect
    data class OnUrlTextFieldValueChange(val value: String) : NonDirect
    data class OnSaveUserProfilePicture(val value: Uri) : NonDirect
    data class OnImageLoading(val value: Boolean) : NonDirect
    data object OnSignOut : NonDirect
    data object OnGetUserData : NonDirect
}