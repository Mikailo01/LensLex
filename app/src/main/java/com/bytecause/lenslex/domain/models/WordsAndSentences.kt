package com.bytecause.lenslex.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class WordsAndSentences(
    val id: String = "",
    val word: String = "",
    val languageCode: String = "",
    val translations: Map<String, String> = emptyMap(),
    val timeStamp: Long = 0L
)