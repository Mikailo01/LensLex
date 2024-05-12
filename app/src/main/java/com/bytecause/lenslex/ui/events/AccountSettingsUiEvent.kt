package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.Provider

// I defined Direct and NonDirect sealed interfaces to get rid of else statement in when expressions and make
// it clear, where should be each event be handled (Direct = directly inside composable, NonDirect = inside viewmodel)
sealed interface AccountSettingsUiEvent {

    sealed interface Direct : AccountSettingsUiEvent
    sealed interface NonDirect : AccountSettingsUiEvent

    data object OnNavigateBack : Direct
    data class OnShowSnackBar(val message: String) : Direct

    data object OnDeleteAccountButtonClick : NonDirect
    data object OnConfirmConfirmationDialog : NonDirect
    data object OnDismissConfirmationDialog : NonDirect
    data class OnShowCredentialDialog(val value: CredentialType) : NonDirect
    data class OnLinkButtonClick(val value: Provider) : NonDirect
    data class OnEnteredCredential(val value: Credentials) : NonDirect
    data class OnCredentialsDialogDismiss(val value: CredentialType) : NonDirect
    data class OnDialogCredentialChanged(val value: Credentials.Sensitive) : NonDirect
}