package com.bytecause.lenslex.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bytecause.lenslex.data.local.datastore.UserPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject


class UserPrefsRepositoryImpl @Inject constructor(
    private val userDataStorePreferences: DataStore<Preferences>
) : UserPrefsRepository {

    override suspend fun saveUserLocale(locale: String) {
        withContext(Dispatchers.IO) {
            userDataStorePreferences.edit { preferences ->
                preferences[USER_LOCALE_KEY] = locale
            }
        }
    }

    override fun loadUserLocale(): Flow<String?> = flow {
        emit(userDataStorePreferences.data.firstOrNull()?.get(USER_LOCALE_KEY))
    }
        .flowOn(Dispatchers.IO)
        .catch { exception ->
            if (exception is IOException) emit(null)
            else throw exception
        }

    override suspend fun saveTranslationOption(langCode: String) {
        withContext(Dispatchers.IO) {
            userDataStorePreferences.edit { preferences ->
                preferences[TRANSLATION_OPTION_KEY] = langCode
            }
        }
    }

    override fun loadTranslationOption(): Flow<String?> = flow {
        emit(userDataStorePreferences.data.firstOrNull()?.get(TRANSLATION_OPTION_KEY))
    }
        .flowOn(Dispatchers.IO)
        .catch { exception ->
            if (exception is IOException) emit(null)
            else throw exception
        }

    private companion object {
        val USER_LOCALE_KEY = stringPreferencesKey("user_locale")
        val TRANSLATION_OPTION_KEY = stringPreferencesKey("translation_option")
    }
}