package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import com.bytecause.lenslex.domain.models.UserAccountDetails
import com.bytecause.lenslex.ui.interfaces.CredentialChangeResult
import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.util.CredentialValidationResult

data class AccountSettingsState(
    val userDetails: UserAccountDetails? = null,
    val linkedProviders: List<Provider> = emptyList(),
    val credentialValidationResult: CredentialValidationResult? = null,
    val credentialChangeResult: CredentialChangeResult? = null,
    val showCredentialUpdateDialog: CredentialType? = null,
    val showConfirmationDialog: AccountSettingsConfirmationDialog? = null,
    val launchGoogleIntent: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)

sealed interface AccountSettingsConfirmationDialog {
    data object DeleteAccountWarning : AccountSettingsConfirmationDialog
    data object PasswordChangeWarning : AccountSettingsConfirmationDialog
}
