package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.ui.interfaces.Credentials

sealed interface LoginUiEvent {
    data class OnCredentialChanged(val value: Credentials.Sensitive) : LoginUiEvent
    data class OnEmailValueChange(val value: String) : LoginUiEvent
    data class OnPasswordValueChange(val value: String) : LoginUiEvent
    data class OnConfirmPasswordChange(val value: String) : LoginUiEvent
    data object OnCredentialsEntered : LoginUiEvent
    data object OnPasswordsVisibilityChange : LoginUiEvent
    data object OnAnnotatedStringClick : LoginUiEvent
    data object OnForgetPasswordClick : LoginUiEvent
    data object OnSignInUsingGoogle : LoginUiEvent
    data object OnSignInAnonymously : LoginUiEvent
}