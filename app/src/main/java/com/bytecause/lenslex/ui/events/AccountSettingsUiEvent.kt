package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.ui.interfaces.Provider

sealed interface AccountSettingsUiEvent {

    data object OnDeleteAccountButtonClick : AccountSettingsUiEvent
    data object OnConfirmConfirmationDialog : AccountSettingsUiEvent
    data object OnDismissConfirmationDialog : AccountSettingsUiEvent
    data class OnShowCredentialDialog(val value: CredentialType) : AccountSettingsUiEvent
    data class OnLinkButtonClick(val value: Provider) : AccountSettingsUiEvent
    data class OnEnteredCredential(val value: Credentials) : AccountSettingsUiEvent
    data class OnCredentialsDialogDismiss(val value: CredentialType) : AccountSettingsUiEvent
    data class OnDialogCredentialChanged(val value: Credentials.Sensitive) : AccountSettingsUiEvent
}