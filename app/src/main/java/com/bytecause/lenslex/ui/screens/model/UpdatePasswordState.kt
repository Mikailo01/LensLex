package com.bytecause.lenslex.ui.screens.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.bytecause.lenslex.util.CredentialValidationResult

@Immutable
data class UpdatePasswordState(
    val password: String = "",
    val confirmationPassword: String = "",
    val passwordVisible: Boolean = false,
    val oobCode: String? = null,
    val showOobCodeExpiredDialog: Boolean = false,
    val animationFinished: Boolean = false,
    val dismissExpiredDialog: Boolean = false,
    val codeValidationResult: UpdatePasswordViewModel.CodeValidationResult? = UpdatePasswordViewModel.CodeValidationResult(
        isLoading = true
    ),
    val credentialValidationResult: CredentialValidationResult? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
