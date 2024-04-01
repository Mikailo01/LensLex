package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    supportedLanguagesRepository: SupportedLanguagesRepository,
    private val userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    private val wordsDatabaseRepository: WordsDatabaseRepository
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

    fun saveTranslationOption(language: SupportedLanguage) {
        viewModelScope.launch {
            userPrefsRepositoryImpl.saveTranslationOption(language.langCode)
            super.setLangOption(language = language)
        }
    }

    fun insertOrUpdateWordAndSentenceEntity(word: WordAndSentenceEntity) {
        viewModelScope.launch {
            wordsDatabaseRepository.insertOrUpdateWordAndSentenceEntity(word)
        }
    }

    val getAllWords: Flow<List<WordAndSentenceEntity>> =
        wordsDatabaseRepository.getAllWords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun deleteWordById(id: Long) {
        viewModelScope.launch {
            wordsDatabaseRepository.deleteWordById(id)
        }
    }
}