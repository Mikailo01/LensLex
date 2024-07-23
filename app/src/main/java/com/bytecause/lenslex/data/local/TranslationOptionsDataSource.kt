package com.bytecause.lenslex.data.local

import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// I created this data source to ensure data consistency across destinations, so when the user change
// language options, it will be the same on every screen
class TranslationOptionsDataSource {

    private val _translationLangOption: MutableStateFlow<Pair<TranslationOption.Origin, TranslationOption.Target>> =
        MutableStateFlow(
            TranslationOption.Origin(SupportedLanguage()) to TranslationOption.Target(
                SupportedLanguage()
            )
        )
    val translationLangOption: StateFlow<Pair<TranslationOption.Origin, TranslationOption.Target>> =
        _translationLangOption.asStateFlow()

    fun updateOption(languageOptions: Pair<TranslationOption.Origin?, TranslationOption.Target?>) {
        when {
            languageOptions.first == null -> {
                languageOptions.second?.let { option ->
                    _translationLangOption.update {
                        it.copy(second = option)
                    }
                }
            }

            languageOptions.second == null -> {
                languageOptions.first?.let { option ->
                    _translationLangOption.update {
                        it.copy(first = option)
                    }
                }
            }

            else -> {
                _translationLangOption.update {
                    it.copy(first = languageOptions.first!!, second = languageOptions.second!!)
                }
            }
        }
    }
}