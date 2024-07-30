package com.bytecause.lenslex.data.repository.abstraction

import com.bytecause.lenslex.domain.models.Words
import kotlinx.coroutines.flow.Flow

interface WordsRepository {
    fun getWords(originLangCode: String, targetLangCode: String): Flow<List<Words>>
    fun addWord(word: Words): Flow<Boolean>
    fun deleteWord(documentId: String)
}