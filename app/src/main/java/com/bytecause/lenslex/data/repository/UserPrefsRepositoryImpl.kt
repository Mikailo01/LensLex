package com.bytecause.lenslex.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException


class UserPrefsRepositoryImpl(
    private val userDataStorePreferences: DataStore<Preferences>,
    private val coroutineDispatcher: CoroutineDispatcher
) : UserPrefsRepository {
    override suspend fun saveOriginTranslationOption(langCode: String) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[ORIGIN_TRANSLATION_OPTION_KEY] = langCode
            }
        }
    }

    override fun loadOriginTranslationOption(): Flow<String?> = flow {
        emit(userDataStorePreferences.data.firstOrNull()?.get(ORIGIN_TRANSLATION_OPTION_KEY))
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            else throw exception
        }

    override suspend fun saveTargetTranslationOption(langCode: String) {
        withContext(coroutineDispatcher) {
            userDataStorePreferences.edit { preferences ->
                preferences[TARGET_TRANSLATION_OPTION_KEY] = langCode
            }
        }
    }

    override fun loadTargetTranslationOption(): Flow<String?> = flow {
        emit(userDataStorePreferences.data.firstOrNull()?.get(TARGET_TRANSLATION_OPTION_KEY))
    }
        .flowOn(coroutineDispatcher)
        .catch { exception ->
            if (exception is IOException) emit(null)
            else throw exception
        }

    override suspend fun setFeatureVisited(featureName: String) {
        withContext(coroutineDispatcher) {
            val featureKey = booleanPreferencesKey(featureName)
            userDataStorePreferences.edit { preferences ->
                preferences[featureKey] = true
            }
        }
    }

    override fun isFeatureVisited(featureName: String): Flow<Boolean> {
        val featureKey = booleanPreferencesKey(featureName)
        return userDataStorePreferences.data.map { preferences ->
            preferences[featureKey] ?: false
        }
            .flowOn(coroutineDispatcher)
            .catch { exception ->
                if (exception is IOException) emit(false)
                else throw exception
            }
    }

    companion object {
        private val ORIGIN_TRANSLATION_OPTION_KEY = stringPreferencesKey("origin_translation_option")
        private val TARGET_TRANSLATION_OPTION_KEY = stringPreferencesKey("target_translation_option")

        const val HOME_FEATURE = "home_feature"
        const val EXTRACTED_TEXT_FEATURE = "extracted_text_feature"
    }
}