package com.bytecause.lenslex.ui.interfaces


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

        data class EmailCredential(
            val email: String
        ) : Sensitive

        data class PasswordCredential(
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