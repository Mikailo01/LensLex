package com.bytecause.lenslex.ui.events

import java.lang.Exception

sealed interface UpdatePasswordUiEvent {
    data class OnVerifyOob(val oobCode: String) : UpdatePasswordUiEvent
    data class OnPasswordValueChange(val password: String) : UpdatePasswordUiEvent
    data class OnConfirmPasswordValueChange(val confirmPassword: String) : UpdatePasswordUiEvent
    data object OnAnimationFinished : UpdatePasswordUiEvent
    data object OnPasswordVisibilityClick : UpdatePasswordUiEvent
    data object OnResetPasswordClick : UpdatePasswordUiEvent
    data object OnTryAgainClick : UpdatePasswordUiEvent
    data object OnGetNewResetCodeClick : UpdatePasswordUiEvent
    data object OnDismiss : UpdatePasswordUiEvent
}

sealed interface UpdatePasswordUiEffect {
    data object GetNewResetCodeClick : UpdatePasswordUiEffect
    data object ResetSuccessful : UpdatePasswordUiEffect
    data class ResetFailure(val exception: Exception?) : UpdatePasswordUiEffect
}