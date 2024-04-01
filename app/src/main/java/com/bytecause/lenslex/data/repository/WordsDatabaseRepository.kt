package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.room.WordDao
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WordsDatabaseRepository @Inject constructor(
    private val wordDao: WordDao
) {

    suspend fun insertOrUpdateWordAndSentenceEntity(entity: WordAndSentenceEntity) {
        withContext(Dispatchers.IO) {
            val existingEntity = wordDao.getWordAndSentenceEntityByWords(entity.words).firstOrNull()
            if (existingEntity == null) {
                wordDao.insertWord(entity)
            } else if (existingEntity.translations != entity.translations) {
                val mergedTranslations = existingEntity.translations.toMutableMap().apply {
                    putAll(entity.translations)
                }
                wordDao.updateWordAndSentenceEntity(existingEntity.copy(translations = mergedTranslations))
            }
        }
    }

    suspend fun deleteWordById(id: Long) {
        withContext(Dispatchers.IO) {
            wordDao.deleteWordById(id)
        }
    }

    val getAllWords: Flow<List<WordAndSentenceEntity>> = wordDao.getAllWords()
}