package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
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
class AddViewModel @Inject constructor(
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    private val supportedLanguagesRepository: SupportedLanguagesRepository
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

    fun setLangOption(langName: String) {
        translationLangOption.value = langName
    }

    val supportedLanguages: List<String> =
        supportedLanguagesRepository.supportedLanguageCodes.map { languageCode ->
            val locale = Locale(languageCode)
            locale.displayLanguage
        }.sortedBy { it }

    fun insertWord(word: WordAndSentenceEntity) {
        viewModelScope.launch {
            wordsDatabaseRepository.insertWord(word)
        }
    }
}