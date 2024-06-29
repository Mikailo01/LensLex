package com.bytecause.lenslex.domain.models


data class SupportedLanguage(
    val langCode: String = "",
    val langName: String = "",
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false
)