package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.local.SupportedLanguagesLocalDataSource

class SupportedLanguagesRepository {

    val supportedLanguageCodes: List<String> =
        SupportedLanguagesLocalDataSource.supportedLanguageCodes
}