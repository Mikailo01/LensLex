package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.Words
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class FakeWordsRepositoryImpl : WordsRepository {

    private val _wordsFlow = MutableStateFlow<List<Words>>(emptyList())

    override fun getWords(
        originLangCode: String,
        targetLangCode: String
    ): Flow<List<Words>> = _wordsFlow.asStateFlow()

    override fun addWord(word: Words): Flow<Boolean> = flow {
        _wordsFlow.value += word
        emit(true)
    }

    override fun deleteWord(documentId: String) {
        _wordsFlow.update {
            it.filterNot { word -> word.id == documentId }
        }
    }
}