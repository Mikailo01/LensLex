package com.bytecause.lenslex.models.uistate

import com.bytecause.lenslex.models.UserData

data class AccountState(
    val userData: UserData? = null,
    val isEditing: Boolean = false,
    val urlValue: String = "",
    val showConfirmationDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showBottomSheet: Boolean = false,
    val showUrlDialog: Boolean = false,
    val signedOutSuccess: Boolean = false
)