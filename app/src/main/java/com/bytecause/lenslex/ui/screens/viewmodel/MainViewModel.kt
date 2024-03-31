package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.util.capital
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    supportedLanguagesRepository: SupportedLanguagesRepository,
    private val userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    private val wordsDatabaseRepository: WordsDatabaseRepository
) : ViewModel() {

    private val translationDataStoreLangOption: Flow<String?> =
        userPrefsRepositoryImpl.loadTranslationOption()

    private val translationLangOption: MutableStateFlow<String> = MutableStateFlow("")

    val languageOptionFlow = combine(
        translationDataStoreLangOption,
        translationLangOption
    ) { dataStoreValue, defaultValue ->

        defaultValue.takeIf { it != "" }?.capital() ?: dataStoreValue?.capital()
        ?: Locale.getDefault().displayName.capital()
    }

    var setShowLanguageDialog by mutableStateOf(false)
        private set

    fun onSelectLanguageClick() {
        setShowLanguageDialog = true
    }

    fun onDismissDialog() {
        setShowLanguageDialog = false
    }

    val supportedLanguages: List<String> =
        supportedLanguagesRepository.supportedLanguageCodes.map { languageCode ->
            val locale = Locale(languageCode)
            locale.displayLanguage
        }.sortedBy { it }

    fun saveTranslationOption(langCode: String) {
        viewModelScope.launch {
            userPrefsRepositoryImpl.saveTranslationOption(langCode)
            translationLangOption.emit(Locale(langCode).displayLanguage)
        }
    }
}