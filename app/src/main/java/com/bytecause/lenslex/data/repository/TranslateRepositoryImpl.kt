package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import kotlinx.coroutines.flow.Flow

class TranslateRepositoryImpl : TranslateRepository {
    override fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Flow<Translator.TranslationResult> = Translator.translate(text, sourceLang, targetLang)
}