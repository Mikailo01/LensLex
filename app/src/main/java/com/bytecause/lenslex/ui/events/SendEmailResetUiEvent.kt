package com.bytecause.lenslex.ui.events

sealed interface SendEmailResetUiEvent {

    data class OnEmailValueChanged(val value: String) : SendEmailResetUiEvent
    data object OnSendEmailClick : SendEmailResetUiEvent
    data object OnAnimationStarted : SendEmailResetUiEvent
}