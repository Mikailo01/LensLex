package com.bytecause.lenslex.ui.screens.viewmodel.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

abstract class BaseViewModel(
    private val userPrefsRepository: UserPrefsRepository,
    private val translationModelManager: TranslationModelManager,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : ViewModel() {

    private val translationDataStoreOriginLangOption: Flow<String?> =
        userPrefsRepository.loadOriginTranslationOption()

    private val translationDataStoreTargetLangOption: Flow<String?> =
        userPrefsRepository.loadTargetTranslationOption()

    private val translationLangOption: MutableStateFlow<Pair<TranslationOption.Origin, TranslationOption.Target>> =
        MutableStateFlow(
            TranslationOption.Origin(SupportedLanguage()) to TranslationOption.Target(
                SupportedLanguage()
            )
        )

    val languageOptionFlow: Flow<Pair<TranslationOption.Origin, TranslationOption.Target>> =
        combine(
            translationDataStoreOriginLangOption,
            translationDataStoreTargetLangOption,
            translationLangOption
        ) { originOption, targetOption, defaultValue ->
            defaultValue.takeIf {
                // take only if state doesn't equal to initial state
                it != TranslationOption.Origin(SupportedLanguage()) to TranslationOption.Target(
                    SupportedLanguage()
                )
            }
                ?: originOption?.let { origin ->
                    targetOption?.let { target ->

                        // Sync state in translationLangOption state flow
                        translationLangOption.update {
                            TranslationOption.Origin(
                                SupportedLanguage(
                                    origin,
                                    Locale(origin).displayName
                                )
                            ) to TranslationOption.Target(
                                SupportedLanguage(
                                    target,
                                    Locale(target).displayName
                                )
                            )
                        }

                        TranslationOption.Origin(
                            SupportedLanguage(
                                langCode = origin,
                                langName = Locale(origin).displayName
                            )
                        ) to TranslationOption.Target(
                            SupportedLanguage(
                                langCode = target,
                                langName = Locale(target).displayName
                            )
                        )
                    }
                }
                ?: (TranslationOption.Origin(SupportedLanguage()) to TranslationOption.Target(
                    SupportedLanguage()
                ))
        }

    private fun setLangOption(language: TranslationOption) {
        translationLangOption.update {
            when (language) {
                is TranslationOption.Origin -> {
                    it.copy(first = language)
                }

                is TranslationOption.Target -> {
                    it.copy(second = language)
                }
            }
        }
    }

    fun saveTranslationOption(language: TranslationOption) {
        viewModelScope.launch {
            // Check which language option changed
            when (language) {
                is TranslationOption.Origin -> {
                    userPrefsRepository.saveOriginTranslationOption(language.lang.langCode)
                }

                is TranslationOption.Target -> {
                    userPrefsRepository.saveTargetTranslationOption(language.lang.langCode)
                }
            }
            setLangOption(language = language)
        }
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
                        Log.d("idk", "success")
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