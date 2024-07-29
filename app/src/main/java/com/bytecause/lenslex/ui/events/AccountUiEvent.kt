package com.bytecause.lenslex.ui.events

import android.net.Uri
import com.bytecause.lenslex.navigation.Screen

sealed interface AccountUiEvent {
    data class OnNavigate(val destination: Screen) : AccountUiEvent
    data class OnUpdateProfilePicture(val value: String) : AccountUiEvent
    data class OnChangeFirebaseLanguage(val value: String) : AccountUiEvent
    data class OnShowConfirmationDialog(val value: Boolean) : AccountUiEvent
    data class OnShowLanguageDialog(val value: Boolean) : AccountUiEvent
    data class OnShowBottomSheet(val value: Boolean) : AccountUiEvent
    data class OnShowUrlDialog(val value: Boolean) : AccountUiEvent
    data class OnNameTextFieldValueChange(val value: String) : AccountUiEvent
    data class OnUrlTextFieldValueChange(val value: String) : AccountUiEvent
    data class OnSaveUserProfilePicture(val value: Uri) : AccountUiEvent
    data class OnImageLoading(val value: Boolean) : AccountUiEvent
    data object OnBackButtonClick : AccountUiEvent
    data object OnSinglePicturePickerLaunch : AccountUiEvent
    data object OnSignOut : AccountUiEvent
    data object OnGetUserData : AccountUiEvent
}

sealed interface AccountUiEffect {
    data class NavigateTo(val screen: Screen) : AccountUiEffect
    data object NavigateBack : AccountUiEffect
    data object SinglePicturePickerLaunch : AccountUiEffect
}