package com.bytecause.lenslex.domain.models

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
