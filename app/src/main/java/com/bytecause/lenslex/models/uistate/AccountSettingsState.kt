package com.bytecause.lenslex.models.uistate

import com.bytecause.lenslex.models.UserAccountDetails
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
    val showConfirmationDialog: Boolean = false
)
