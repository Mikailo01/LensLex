package com.bytecause.lenslex.data.repository.abstraction

import kotlinx.coroutines.flow.Flow


interface UserPrefsRepository {
    suspend fun saveTranslationOption(langName: String)
    fun loadTranslationOption(): Flow<String?>
}