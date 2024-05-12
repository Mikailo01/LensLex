package com.bytecause.lenslex.ui.screens.uistate

import com.bytecause.lenslex.util.CredentialValidationResult

data class LoginState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val signIn: Boolean = true,
    val isLoading: Boolean = false,
    val credentialValidationResult: CredentialValidationResult? = null
)
