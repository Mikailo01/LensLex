package com.bytecause.lenslex.data.repository.abstraction

import com.bytecause.lenslex.domain.models.WordsAndSentences
import kotlinx.coroutines.flow.Flow

interface WordsRepository {
    fun getWords(originLangCode: String, targetLangCode: String): Flow<List<WordsAndSentences>>
    fun addWord(word: WordsAndSentences): Flow<Boolean>
    fun deleteWord(documentId: String)
}