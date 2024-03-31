package com.bytecause.lenslex.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words_and_sentences")
    fun getAllWords(): Flow<WordAndSentenceEntity?>

    @Insert
    suspend fun insertWord(word: WordAndSentenceEntity)
}