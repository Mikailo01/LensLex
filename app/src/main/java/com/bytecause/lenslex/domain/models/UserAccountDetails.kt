package com.bytecause.lenslex.domain.models


data class UserAccountDetails(
    val uid: String,
    val creationTimeStamp: Long?,
    val userName: String?,
    val email: String?,
    val profilePictureUrl: String,
    val isAnonymous: Boolean
)
