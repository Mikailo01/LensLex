package com.bytecause.lenslex.domain.models

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)
