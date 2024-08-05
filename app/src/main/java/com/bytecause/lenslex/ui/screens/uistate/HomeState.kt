package com.bytecause.lenslex.ui.screens.uistate

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.domain.models.Words
import com.bytecause.lenslex.ui.interfaces.TranslationOption

@Immutable
data class HomeState(
    val wordList: List<Words> = emptyList(),
    val profilePictureUrl: String = "",
    val isLoading: Boolean = true,
    val fabState: Boolean = false,
    val isEditEnabled: Boolean = false,
    val showIntroShowcase: Boolean = false,
    val showProgressBar: Boolean = false,
    val languageFilterText: String = "",
    val supportedLanguages: List<SupportedLanguage> = emptyList(),
    val selectedLanguageOptions: Pair<TranslationOption.Origin, TranslationOption.Target> = TranslationOption.Origin(
        SupportedLanguage()
    ) to TranslationOption.Target(SupportedLanguage()),
    val showLanguageDialog: TranslationOption? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val deletedItemsStack: List<Words> = emptyList(),
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val lazyListState: LazyListState = LazyListState(),
    val lazyGridState: LazyGridState = LazyGridState()
)