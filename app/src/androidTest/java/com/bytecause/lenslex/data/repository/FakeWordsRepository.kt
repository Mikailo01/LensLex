package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeWordsRepository : WordsRepository {

    private val words = mutableListOf<WordsAndSentences>()

    override fun getWords(): Flow<List<WordsAndSentences>> = flow { emit(words) }

    override fun addWord(word: WordsAndSentences): Flow<Boolean> = flow {
        words.add(word)
        emit(true)
    }

    override fun deleteWord(documentId: String) {
        words.removeIf { it.id == documentId }
    }

}