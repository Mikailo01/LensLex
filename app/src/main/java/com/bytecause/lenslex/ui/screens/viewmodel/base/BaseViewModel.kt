package com.bytecause.lenslex.ui.screens.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.util.capital
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SupportedLanguage()
    )

    fun setLangOption(language: SupportedLanguage) {
        translationLangOption.value = language
    }

    val supportedLanguages: List<SupportedLanguage> =
        supportedLanguagesRepository.supportedLanguageCodes
}