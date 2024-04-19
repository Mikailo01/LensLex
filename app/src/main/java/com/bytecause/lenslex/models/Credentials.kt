package com.bytecause.lenslex.models


sealed interface Credentials {

    sealed interface Sensitive : Credentials {
        data class SignInCredentials(
            val email: String,
            val password: String
        ) : Sensitive

        data class SignUpCredentials(
            val email: String,
            val password: String,
            val confirmPassword: String
        ) : Sensitive

        data class EmailUpdateCredential(
            val email: String
        ) : Sensitive

        data class PasswordUpdateCredential(
            val password: String,
            val confirmPassword: String
        ) : Sensitive
    }

    sealed interface Insensitive : Credentials {
        data class UsernameUpdate(
            val username: String
        ) : Insensitive
    }
}