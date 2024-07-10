package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeUserPrefsRepositoryImpl : UserPrefsRepository {
    private var originLangOption = ""
    private var targetLangOption = ""

    override suspend fun saveOriginTranslationOption(langCode: String) {
        originLangOption = langCode
    }

    override suspend fun saveTargetTranslationOption(langCode: String) {
        targetLangOption = langCode
    }

    override fun loadOriginTranslationOption(): Flow<String?> = flow { emit(originLangOption) }
    override fun loadTargetTranslationOption(): Flow<String?> = flow { emit(targetLangOption) }

    override suspend fun setFeatureVisited(featureName: String) {
        TODO("Not yet implemented")
    }

    override fun isFeatureVisited(featureName: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }
}