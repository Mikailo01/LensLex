package com.bytecause.lenslex.ui.events

import android.net.Uri

sealed interface ModifiedImagePreviewUiEvent {
    data class OnUpdateImage(val uri: Uri) : ModifiedImagePreviewUiEvent
    data object OnLaunchCropLauncher : ModifiedImagePreviewUiEvent
    data object OnProcessImageClick : ModifiedImagePreviewUiEvent
}

sealed interface ModifiedImagePreviewUiEffect {
    data object ImageTextless : ModifiedImagePreviewUiEffect
    data object LaunchCropLauncher : ModifiedImagePreviewUiEffect
    data class NavigateWithTextResult(val text: List<String>) : ModifiedImagePreviewUiEffect
}