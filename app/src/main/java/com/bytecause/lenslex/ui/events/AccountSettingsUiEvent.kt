package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.Provider
import com.google.firebase.auth.AuthCredential

sealed interface AccountSettingsUiEvent {
    data object OnNavigateBack : AccountSettingsUiEvent
    data object OnDeleteAccountButtonClick : AccountSettingsUiEvent
    data object OnConfirmConfirmationDialog : AccountSettingsUiEvent
    data object OnDismissConfirmationDialog : AccountSettingsUiEvent
    data object OnLaunchReauthenticationGoogleIntent : AccountSettingsUiEvent
    data object OnCredentialsDialogDismiss : AccountSettingsUiEvent
    data class OnShowSnackBar(val message: String) : AccountSettingsUiEvent
    data class OnLinkGoogleProvider(val value: AuthCredential) : AccountSettingsUiEvent
    data class OnReauthenticateWithGoogle(val value: AuthCredential) : AccountSettingsUiEvent
    data class OnShowCredentialDialog(val value: CredentialType) : AccountSettingsUiEvent
    data class OnLinkButtonClick(val value: Provider) : AccountSettingsUiEvent
    data class OnEnteredCredential(val value: Credentials) : AccountSettingsUiEvent
    data class OnDialogCredentialChanged(val value: Credentials.Sensitive) : AccountSettingsUiEvent
}

sealed interface AccountSettingsUiEffect {
    data object LinkGoogleProvider : AccountSettingsUiEffect
    data object ReauthenticateWithGoogleProvider : AccountSettingsUiEffect
    data object NavigateBack : AccountSettingsUiEffect
    data class AccountActionResult(val result: com.bytecause.lenslex.ui.interfaces.AccountActionResult) : AccountSettingsUiEffect
    data class ShowMessage(val message: String) : AccountSettingsUiEffect
}