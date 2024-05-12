package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.SupportedLanguagesLocalDataSource
import com.bytecause.lenslex.domain.models.SupportedLanguage
import java.util.Locale

class SupportedLanguagesRepository {

    val supportedLanguageCodes: List<SupportedLanguage> =
        SupportedLanguagesLocalDataSource.supportedLanguageCodes.map { langCode ->
            SupportedLanguage(
                langCode = langCode,
                langName = Locale(langCode).displayLanguage
            )
        }.sortedBy { it.langName }
}