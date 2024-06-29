package com.bytecause.lenslex.ui.models

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
