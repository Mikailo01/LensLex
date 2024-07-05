package com.bytecause.lenslex.ui.screens.uistate

import android.net.Uri
import androidx.compose.material3.SnackbarHostState

data class ModifiedImagePreviewState(
    val modifiedImageUri: Uri = Uri.EMPTY,
    val isProcessing: Boolean = false,
    val isButtonEnabled: Boolean = true,
    val isImageTextless: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)