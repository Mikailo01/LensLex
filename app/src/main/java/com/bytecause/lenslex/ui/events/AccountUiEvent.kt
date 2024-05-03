package com.bytecause.lenslex.ui.events

import android.net.Uri

sealed interface AccountUiEvent {
    data class OnUpdateName(val value: String): AccountUiEvent
    data class OnUpdateProfilePicture(val value: String): AccountUiEvent
    data class OnChangeFirebaseLanguage(val value: String): AccountUiEvent
    data class OnShowConfirmationDialog(val value: Boolean): AccountUiEvent
    data class OnShowLanguageDialog(val value: Boolean): AccountUiEvent
    data class OnShowBottomSheet(val value: Boolean): AccountUiEvent
    data class OnShowUrlDialog(val value: Boolean): AccountUiEvent
    data class OnNameTextFieldValueChange(val value: String): AccountUiEvent
    data class OnUrlTextFieldValueChange(val value: String): AccountUiEvent
    data class OnSaveUserProfilePicture(val value: Uri): AccountUiEvent
    data object OnEditChange: AccountUiEvent
    data object OnSignOut: AccountUiEvent
}