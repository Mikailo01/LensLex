package com.bytecause.lenslex.ui.screens.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.mlkit.TranslationModelManager
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.util.capital
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

abstract class BaseViewModel(
    userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : ViewModel() {

    private val translationDataStoreLangOption: Flow<String?> =
        userPrefsRepositoryImpl.loadTranslationOption()

    private val translationLangOption: MutableStateFlow<SupportedLanguage> = MutableStateFlow(
        SupportedLanguage()
    )

    val languageOptionFlow: Flow<SupportedLanguage> = combine(
        translationDataStoreLangOption,
        translationLangOption
    ) { dataStoreValue, defaultValue ->
        defaultValue.takeIf { it != SupportedLanguage() }
            ?: dataStoreValue?.let {
                SupportedLanguage(
                    langCode = it,
                    langName = Locale(it).displayName.split(" ")[0].capital()
                )
            }
            ?: SupportedLanguage(
                langCode = Locale.getDefault().language,
                langName = Locale.getDefault().displayName.capital()
            )
    }

    fun setLangOption(language: SupportedLanguage) {
        translationLangOption.value = language
    }

    private fun getDownloadedModels() {
        TranslationModelManager.getModels { modelSet ->
            _supportedLanguages.value.map { language ->
                language.copy(isDownloaded = modelSet.any { it.language == language.langCode })
            }.let {
                _supportedLanguages.value = it
            }
        }
    }

    fun removeModel(langCode: String) {
        TranslationModelManager.deleteModel(langCode) {
            getDownloadedModels()
        }
    }

    private fun setIsModelDownloading(langCode: String, boolean: Boolean) {
        _supportedLanguages.value = _supportedLanguages.value.map {
            if (it.langCode == langCode) it.copy(isDownloading = boolean)
            else it
        }
    }

    fun downloadModel(
        langCode: String
    ) {
        setIsModelDownloading(langCode, true)

        TranslationModelManager.downloadModel(
            languageTag = langCode,
            onDownloadSuccess = {
                getDownloadedModels()
                setIsModelDownloading(langCode, false)
            },
            onDownloadFailure = { exception ->
                setIsModelDownloading(langCode, false)
            }
        )
    }

    private val _supportedLanguages: MutableStateFlow<List<SupportedLanguage>> = MutableStateFlow(
        supportedLanguagesRepository.supportedLanguageCodes
    )
        .also { getDownloadedModels() }

    val supportedLanguages: StateFlow<List<SupportedLanguage>> = _supportedLanguages.asStateFlow()
}