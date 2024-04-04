package com.bytecause.lenslex.models

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
