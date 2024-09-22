package com.bytecause.lenslex.ui.screens.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.UserAccountDetails
import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.util.CredentialValidationResult

@Immutable
data class AccountSettingsState(
    val userDetails: UserAccountDetails? = null,
    val linkedProviders: List<Provider> = emptyList(),
    val credentialValidationResult: CredentialValidationResult? = null,
    val showCredentialUpdateDialog: CredentialType? = null,
    val showReauthorizationDialog: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val shouldReauthenticate: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
