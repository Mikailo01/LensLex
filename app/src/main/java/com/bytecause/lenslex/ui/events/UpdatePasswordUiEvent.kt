package com.bytecause.lenslex.ui.events

sealed interface UpdatePasswordUiEvent {

    data class OnPasswordValueChange(val password: String) : UpdatePasswordUiEvent
    data class OnConfirmPasswordValueChange(val confirmPassword: String) : UpdatePasswordUiEvent
    data object OnAnimationStarted : UpdatePasswordUiEvent
    data object OnPasswordVisibilityClick : UpdatePasswordUiEvent
    data object OnResetPasswordClick : UpdatePasswordUiEvent
    data object OnTryAgainClick : UpdatePasswordUiEvent
    data object OnGetNewResetCodeClick : UpdatePasswordUiEvent
    data object OnDismiss : UpdatePasswordUiEvent
}