package com.bytecause.lenslex.ui.mappers

import com.bytecause.lenslex.ui.models.Word

// Mapping and formatting function
fun textListToWordList(text: List<String>): List<Word> = text.map { textList ->
    // Split words by whitespaces
    textList.split(
        "\\s+".toRegex()
    )
        .mapNotNull { word ->
            // Filter out all unnecessary symbols
            "[a-zA-Z0-9]+".toRegex().find(word)?.value
        }
        .mapIndexed { index, s -> Word(id = index, text = s.lowercase()) }
}
    .flatten()