package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.TranslationOptionsDataSource
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.abstraction.UserPrefsRepository
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

abstract class TranslationViewModel(
    private val userPrefsRepository: UserPrefsRepository,
    private val translationModelManager: TranslationModelManager,
    private val translationOptionsDataSource: TranslationOptionsDataSource,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : ViewModel() {

    private val translationDataStoreOriginLangOption: Flow<String?> =
        userPrefsRepository.loadOriginTranslationOption()

    private val translationDataStoreTargetLangOption: Flow<String?> =
        userPrefsRepository.loadTargetTranslationOption()

    val languageOptionFlow: Flow<Pair<TranslationOption.Origin, TranslationOption.Target>> =
        combine(
            translationDataStoreOriginLangOption,
            translationDataStoreTargetLangOption,
            translationOptionsDataSource.translationLangOption
        ) { originOption, targetOption, defaultValue ->
            val initialOption =
                TranslationOption.Origin(SupportedLanguage()) to TranslationOption.Target(
                    SupportedLanguage()
                )

            if (defaultValue != initialOption) {
                defaultValue
            } else {
                originOption?.let { origin ->
                    targetOption?.let { target ->
                        // Sync state in translationLangOption state flow
                        translationOptionsDataSource.updateOption(
                            TranslationOption.Origin(
                                SupportedLanguage(origin, Locale(origin).displayName)
                            ) to TranslationOption.Target(
                                SupportedLanguage(target, Locale(target).displayName)
                            )
                        )
                        translationOptionsDataSource.translationLangOption.value
                    }
                }
            } ?: initialOption
        }

    fun saveTranslationOptions(languageOptions: Pair<TranslationOption.Origin?, TranslationOption.Target?>) {
        viewModelScope.launch {
            languageOptions.first?.let { option ->
                // If first lang is the same as second, switch them
                if (option.lang.langCode == translationOptionsDataSource.translationLangOption.value.second.lang.langCode) {
                    translationOptionsDataSource.updateOption(
                        TranslationOption.Origin(translationOptionsDataSource.translationLangOption.value.second.lang)
                            .also { userPrefsRepository.saveOriginTranslationOption(it.lang.langCode) }
                                to TranslationOption.Target(translationOptionsDataSource.translationLangOption.value.first.lang)
                            .also { userPrefsRepository.saveTargetTranslationOption(it.lang.langCode) }
                    )
                    return@launch
                } else userPrefsRepository.saveOriginTranslationOption(option.lang.langCode)
            }

            languageOptions.second?.let { option ->
                // If first lang is the same as second, switch them
                if (option.lang.langCode == translationOptionsDataSource.translationLangOption.value.first.lang.langCode) {
                    translationOptionsDataSource.updateOption(
                        TranslationOption.Origin(translationOptionsDataSource.translationLangOption.value.second.lang)
                            .also { userPrefsRepository.saveOriginTranslationOption(it.lang.langCode) }
                                to
                                TranslationOption.Target(translationOptionsDataSource.translationLangOption.value.first.lang)
                                    .also { userPrefsRepository.saveTargetTranslationOption(it.lang.langCode) }
                    )
                    return@launch
                } else userPrefsRepository.saveTargetTranslationOption(option.lang.langCode)
            }
            translationOptionsDataSource.updateOption(languageOptions)
        }
    }

    fun switchLanguageOptions(origin: TranslationOption.Origin, target: TranslationOption.Target) {
        saveTranslationOptions(
            Pair(TranslationOption.Origin(target.lang), TranslationOption.Target(origin.lang))
        )
    }

    fun areModelsReady(
        origin: TranslationOption.Origin,
        target: TranslationOption.Target
    ): Boolean {
        val isOriginDownloaded =
            supportedLanguages.value.find { it.langCode == origin.lang.langCode }?.isDownloaded
                ?: false
        val isTargetDownloaded =
            supportedLanguages.value.find { it.langCode == target.lang.langCode }?.isDownloaded
                ?: false
        return isOriginDownloaded && isTargetDownloaded
    }

    private fun getDownloadedModels() {
        viewModelScope.launch {
            translationModelManager.getModels().firstOrNull()?.let { result ->
                result
                    .onSuccess { modelSet ->
                        _supportedLanguages.value.map { language ->
                            language.copy(isDownloaded = modelSet.any { it.language == language.langCode })
                        }.let {
                            _supportedLanguages.value = it
                        }
                    }
            }
        }
    }

    fun removeModel(langCode: String) {
        viewModelScope.launch {
            translationModelManager.deleteModel(langCode).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        getDownloadedModels()
                    }
            }
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
        viewModelScope.launch {
            setIsModelDownloading(langCode, true)

            translationModelManager.downloadModel(langCode).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        getDownloadedModels()
                        setIsModelDownloading(langCode, false)
                    }
                    .onFailure {
                        setIsModelDownloading(langCode, false)
                    }
            }
        }
    }

    private val _supportedLanguages: MutableStateFlow<List<SupportedLanguage>> = MutableStateFlow(
        supportedLanguagesRepository.supportedLanguageCodes
    )
        .also { getDownloadedModels() }

    val supportedLanguages: StateFlow<List<SupportedLanguage>> = _supportedLanguages.asStateFlow()
}