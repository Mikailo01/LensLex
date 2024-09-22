package com.bytecause.lenslex.ui.screens.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable

@Immutable
data class SendEmailResetState(
    val email: String = "",
    val isEmailError: Boolean = false,
    val timer: Int = -1,
    val animationFinished: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
