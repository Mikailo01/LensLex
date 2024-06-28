package com.bytecause.lenslex.ui.screens.uistate

import android.net.Uri

data class ModifiedImagePreviewState(
    val modifiedImageUri: Uri = Uri.EMPTY,
    val isProcessing: Boolean = false,
    val isButtonEnabled: Boolean = true,
    val isImageTextless: Boolean = false
)