package com.bytecause.lenslex.ui.screens.uistate

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable

@Immutable
data class ModifiedImagePreviewState(
    val modifiedImageUri: Uri = Uri.EMPTY,
    val isProcessing: Boolean = false,
    val isButtonEnabled: Boolean = true,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)