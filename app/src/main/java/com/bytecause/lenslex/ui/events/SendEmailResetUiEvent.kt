package com.bytecause.lenslex.ui.events

import java.lang.Exception

sealed interface SendEmailResetUiEvent {
    data class OnEmailValueChanged(val value: String) : SendEmailResetUiEvent
    data object OnSendEmailClick : SendEmailResetUiEvent
    data object OnAnimationFinished : SendEmailResetUiEvent
    data object OnNavigateBack : SendEmailResetUiEvent
}

sealed interface SendEmailResetUiEffect {
    data object SuccessfulRequest : SendEmailResetUiEffect
    data class FailureRequest(val exception: Exception?) : SendEmailResetUiEffect
    data object NavigateBack : SendEmailResetUiEffect
}