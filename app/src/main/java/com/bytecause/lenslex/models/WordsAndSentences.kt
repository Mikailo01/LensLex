package com.bytecause.lenslex.models

data class WordsAndSentences(
    val id: String = "",
    val word: String = "",
    val languageCode: String = "",
    val translations: Map<String, String> = emptyMap(),
    val timeStamp: Long = 0L
)