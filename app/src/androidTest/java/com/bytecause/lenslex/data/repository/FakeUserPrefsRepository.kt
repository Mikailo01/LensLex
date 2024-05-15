package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeUserPrefsRepository: UserPrefsRepository {
    private var langOption = ""

    override suspend fun saveTranslationOption(langName: String) {
        langOption = langName
    }
    override fun loadTranslationOption(): Flow<String?> = flow { emit(langOption) }
}