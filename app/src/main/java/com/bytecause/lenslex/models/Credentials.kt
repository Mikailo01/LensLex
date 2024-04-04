package com.bytecause.lenslex.models


sealed interface Credentials {

    data class SignInCredentials(
        val email: String,
        val password: String
    ) : Credentials

    data class SignUpCredentials(
        val email: String,
        val password: String,
        val confirmPassword: String
    ) : Credentials
}