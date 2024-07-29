package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.UserData

@Immutable
data class AccountState(
    val userData: UserData? = UserData(),
    val isImageLoading: Boolean = false,
    val urlValue: String = "",
    val showConfirmationDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showBottomSheet: Boolean = false,
    val showUrlDialog: Boolean = false
)