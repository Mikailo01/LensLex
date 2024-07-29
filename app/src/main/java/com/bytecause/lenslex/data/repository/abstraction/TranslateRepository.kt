package com.bytecause.lenslex.data.repository.abstraction

import com.bytecause.lenslex.data.local.mlkit.Translator
import kotlinx.coroutines.flow.Flow

interface TranslateRepository {
    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Flow<Translator.TranslationResult>
}