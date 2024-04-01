package com.bytecause.lenslex.data.local.datastore

import kotlinx.coroutines.flow.Flow


interface UserPrefsRepository {
    suspend fun saveUserLocale(locale: String)
    fun loadUserLocale(): Flow<String?>

    suspend fun saveTranslationOption(langName: String)
    fun loadTranslationOption(): Flow<String?>
}