package com.bytecause.lenslex.models

data class UserData(
    val userId: String,
    val userName: String?,
    val profilePictureUrl: String?,
    val isAnonymous: Boolean
)