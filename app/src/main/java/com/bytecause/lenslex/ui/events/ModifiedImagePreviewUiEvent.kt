package com.bytecause.lenslex.ui.events

import android.net.Uri

sealed interface ModifiedImagePreviewUiEvent {

    sealed interface Direct : ModifiedImagePreviewUiEvent
    sealed interface NonDirect : ModifiedImagePreviewUiEvent

    data object OnLaunchCropLauncher : Direct
    data class OnUpdateImage(val uri: Uri) : NonDirect
    data object OnProcessImageClick : NonDirect
}