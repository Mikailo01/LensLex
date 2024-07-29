package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Immutable
data class HomeState(
    val wordList: List<WordsAndSentences> = emptyList(),
    val profilePictureUrl: String = "",
    val isLoading: Boolean = false,
    val fabState: Boolean = false,
    val isEditEnabled: Boolean = false,
    val showIntroShowcase: Boolean = false,
    val showProgressBar: Boolean = false,
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val deletedItemsStack: List<WordsAndSentences> = emptyList(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val lazyListState: LazyListState = LazyListState(),
    val lazyGridState: LazyGridState = LazyGridState()
)