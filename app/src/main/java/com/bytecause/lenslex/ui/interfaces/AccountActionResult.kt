package com.bytecause.lenslex.ui.interfaces

import com.bytecause.lenslex.ui.screens.AccountSettingsMessage

sealed interface AccountActionResult {
    data class Success(val message: AccountSettingsMessage) : AccountActionResult
    sealed class Failure : AccountActionResult {
        data object ReauthorizationRequired : Failure()
        data class Error(val exception: Throwable) : Failure()
    }
}