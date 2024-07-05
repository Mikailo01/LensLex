package com.bytecause.lenslex.ui.interfaces

import androidx.annotation.StringRes

sealed interface CredentialChangeResult {
    data class Success(@StringRes val message: Int) : CredentialChangeResult
    sealed class Failure : CredentialChangeResult {
        data class ReauthorizationRequired(
            val email: String? = null,
            val password: String? = null,
            val deleteAccount: Boolean = false
        ) : Failure()

        data class Error(val exception: Throwable) : Failure()
    }
}