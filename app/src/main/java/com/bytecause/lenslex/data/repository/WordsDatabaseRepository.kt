package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.room.WordDao
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WordsDatabaseRepository @Inject constructor(
    private val wordDao: WordDao
) {

    suspend fun insertWord(word: WordAndSentenceEntity) {
        withContext(Dispatchers.IO) {
            wordDao.insertWord(word)
        }
    }

    fun getAllWords(): Flow<WordAndSentenceEntity?> = wordDao.getAllWords()
}