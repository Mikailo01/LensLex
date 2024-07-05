package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.bytecause.lenslex.util.CredentialValidationResult

data class UpdatePasswordState(
    val password: String = "",
    val confirmationPassword: String = "",
    val passwordVisible: Boolean = false,
    val oobCode: String? = null,
    val showOobCodeExpiredDialog: Boolean = false,
    val animationStarted: Boolean = false,
    val dismissExpiredDialog: Boolean = false,
    val resetState: SimpleResult? = null,
    val codeValidationResult: UpdatePasswordViewModel.CodeValidationResult? = UpdatePasswordViewModel.CodeValidationResult(
        isLoading = true
    ),
    val credentialValidationResult: CredentialValidationResult? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
