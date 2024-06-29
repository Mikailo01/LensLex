package com.bytecause.lenslex.ui.models

import com.bytecause.lenslex.domain.models.UserData

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)
