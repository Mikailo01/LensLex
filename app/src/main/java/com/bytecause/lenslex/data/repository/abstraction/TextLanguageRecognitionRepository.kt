package com.bytecause.lenslex.data.repository.abstraction

import kotlinx.coroutines.flow.Flow

interface TextLanguageRecognitionRepository {
    fun runLangRecognition(text: String): Flow<String>
}