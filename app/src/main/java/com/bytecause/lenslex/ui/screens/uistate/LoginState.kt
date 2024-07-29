package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.ui.models.SignInState
import com.bytecause.lenslex.util.CredentialValidationResult

@Immutable
data class LoginState(
    val signInState: SignInState = SignInState(),
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val signIn: Boolean = true,
    val isLoading: Boolean = false,
    val animationFinished: Boolean = false,
    val credentialManagerShown: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val credentialValidationResult: CredentialValidationResult? = null
)
