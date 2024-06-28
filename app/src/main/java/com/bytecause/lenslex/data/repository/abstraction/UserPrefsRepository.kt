package com.bytecause.lenslex.data.repository.abstraction

import kotlinx.coroutines.flow.Flow


interface UserPrefsRepository {
    suspend fun saveOriginTranslationOption(langCode: String)
    suspend fun saveTargetTranslationOption(langCode: String)
    fun loadOriginTranslationOption(): Flow<String?>
    fun loadTargetTranslationOption(): Flow<String?>
}