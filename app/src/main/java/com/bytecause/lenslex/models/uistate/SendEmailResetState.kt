package com.bytecause.lenslex.models.uistate

import com.bytecause.lenslex.ui.interfaces.SimpleResult

data class SendEmailResetState(
    val email: String = "",
    val isEmailError: Boolean = false,
    val timer: Int = -1,
    val animationStarted: Boolean = false,
    val requestResult: SimpleResult? = null
)
