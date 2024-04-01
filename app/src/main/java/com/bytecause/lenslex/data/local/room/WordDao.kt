package com.bytecause.lenslex.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words_and_sentences")
    fun getAllWords(): Flow<List<WordAndSentenceEntity>>

    @Update
    suspend fun updateWordAndSentenceEntity(entity: WordAndSentenceEntity)

    @Query("DELETE FROM words_and_sentences WHERE id = :id")
    suspend fun deleteWordById(id: Long)

    @Query("SELECT * FROM words_and_sentences WHERE words = :words")
    fun getWordAndSentenceEntityByWords(words: String): Flow<WordAndSentenceEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: WordAndSentenceEntity)
}