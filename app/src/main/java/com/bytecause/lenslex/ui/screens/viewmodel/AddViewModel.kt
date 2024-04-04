package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddViewModel @Inject constructor(
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

    fun insertOrUpdateWordAndSentenceEntity(entity: WordAndSentenceEntity) {
        viewModelScope.launch {
            // Remove unnecessary whitespaces.
            val regex = Regex("\\s+")
            wordsDatabaseRepository.insertOrUpdateWordAndSentenceEntity(
                entity.copy(
                    word = entity.word.replace(regex, " ").lowercase()
                )
            )
        }
    }
}