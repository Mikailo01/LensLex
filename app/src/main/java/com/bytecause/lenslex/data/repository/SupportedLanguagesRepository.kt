package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.SupportedLanguagesLocalDataSource
import com.bytecause.lenslex.domain.models.SupportedLanguage
import com.bytecause.lenslex.util.capital
import java.util.Locale

class SupportedLanguagesRepository {
    fun supportedLanguageCodes(): List<SupportedLanguage> =
        SupportedLanguagesLocalDataSource.supportedLanguageCodes.map { langCode ->
            SupportedLanguage(
                langCode = langCode,
                langName = Locale(langCode).displayLanguage.capital()
            )
        }.sortedBy { it.langName }
}