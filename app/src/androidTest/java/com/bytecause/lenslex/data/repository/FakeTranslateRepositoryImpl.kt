package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.repository.abstraction.TranslateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeTranslateRepositoryImpl : TranslateRepository {
    override fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Flow<Translator.TranslationResult> =
        flow { emit(Translator.TranslationResult.TranslationSuccess("translated text")) }
}