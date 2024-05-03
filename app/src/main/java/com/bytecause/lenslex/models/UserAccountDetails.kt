package com.bytecause.lenslex.models

import androidx.compose.runtime.Stable

@Stable
data class UserAccountDetails(
    val uid: String,
    val creationTimeStamp: Long?,
    val userName: String,
    val email: String,
    val isAnonymous: Boolean
)
