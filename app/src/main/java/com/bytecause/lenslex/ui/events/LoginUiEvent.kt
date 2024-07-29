package com.bytecause.lenslex.ui.events

import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.models.SignInResult

sealed interface LoginUiEvent {
    data class OnForgetPasswordClick(val screen: Screen) : LoginUiEvent
    data class OnEmailValueChange(val email: String) : LoginUiEvent
    data class OnPasswordValueChange(val password: String) : LoginUiEvent
    data class OnConfirmPasswordValueChange(val confirmationPassword: String) : LoginUiEvent
    data class OnUpdateSignInResult(val value: SignInResult) : LoginUiEvent
    data class OnSignInUsingEmailAndPassword(val credentials: Credentials.Sensitive.SignInCredentials) :
        LoginUiEvent

    data object OnSignInUsingGoogle : LoginUiEvent
    data object OnCredentialsEntered : LoginUiEvent
    data object OnPasswordsVisibilityChange : LoginUiEvent
    data object OnAnnotatedStringClick : LoginUiEvent
    data object OnSignInAnonymously : LoginUiEvent
    data object OnAnimationFinished : LoginUiEvent
    data object OnCredentialManagerShown : LoginUiEvent
}

sealed interface LoginUiEffect {
    data object SignInUsingGoogleIntent : LoginUiEffect
    data class NavigateTo(val destination: Screen) : LoginUiEffect
}