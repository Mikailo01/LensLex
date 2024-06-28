package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class FakeWordsRepositoryImpl : WordsRepository {

    private val _wordsFlow = MutableStateFlow<List<WordsAndSentences>>(emptyList())

    override fun getWords(): Flow<List<WordsAndSentences>> = _wordsFlow.asStateFlow()

    override fun addWord(word: WordsAndSentences): Flow<Boolean> = flow {
        _wordsFlow.value += word
        emit(true)
    }

    override fun deleteWord(documentId: String) {
        _wordsFlow.update {
            it.filterNot { word -> word.id == documentId }
        }
    }
}