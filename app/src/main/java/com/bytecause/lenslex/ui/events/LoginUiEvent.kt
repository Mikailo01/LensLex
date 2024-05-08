package com.bytecause.lenslex.ui.events

sealed interface LoginUiEvent {
    data class OnEmailValueChange(val email: String) : LoginUiEvent
    data class OnPasswordValueChange(val password: String) : LoginUiEvent
    data class OnConfirmPasswordValueChange(val confirmationPassword: String) : LoginUiEvent
    data object OnCredentialsEntered : LoginUiEvent
    data object OnPasswordsVisibilityChange : LoginUiEvent
    data object OnAnnotatedStringClick : LoginUiEvent
    data object OnForgetPasswordClick : LoginUiEvent
    data object OnSignInUsingGoogle : LoginUiEvent
    data object OnSignInAnonymously : LoginUiEvent
}