package com.bytecause.lenslex.ui.events

// I defined Direct and NonDirect sealed interfaces to get rid of else statement in when expressions and make
// it clear, where should be each event be handled (Direct = directly inside composable, NonDirect = inside viewmodel)
sealed interface LoginUiEvent {

    sealed interface Direct : LoginUiEvent
    sealed interface NonDirect : LoginUiEvent

    data object OnForgetPasswordClick : Direct
    data object OnSignInUsingGoogle : Direct

    data class OnEmailValueChange(val email: String) : NonDirect
    data class OnPasswordValueChange(val password: String) : NonDirect
    data class OnConfirmPasswordValueChange(val confirmationPassword: String) : NonDirect
    data object OnCredentialsEntered : NonDirect
    data object OnPasswordsVisibilityChange : NonDirect
    data object OnAnnotatedStringClick : NonDirect
    data object OnSignInAnonymously : NonDirect
}