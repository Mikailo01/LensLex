package com.bytecause.lenslex.domain.models

import androidx.compose.runtime.Stable

@Stable
data class SupportedLanguage(
    val langCode: String = "",
    val langName: String = "",
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false
)