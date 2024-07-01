package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import kotlinx.coroutines.flow.Flow

class TranslateRepositoryImpl(
    private val translator: Translator
) : TranslateRepository {
    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Flow<Translator.TranslationResult> = translator.translate(text, sourceLang, targetLang)
}