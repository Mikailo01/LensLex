package com.bytecause.lenslex.data.local.room.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bytecause.nautichart.data.local.room.converter.MapTypeConverter

@Entity(tableName = "words_and_sentences")
@TypeConverters(MapTypeConverter::class)
data class WordAndSentenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val words: String = "",
    val languageCode: String = "",
    val translations: Map<String, String> = emptyMap()
)