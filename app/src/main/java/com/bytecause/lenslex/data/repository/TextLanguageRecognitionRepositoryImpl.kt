package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.repository.abstraction.TextLanguageRecognitionRepository
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TextLanguageRecognitionRepositoryImpl : TextLanguageRecognitionRepository {
    override fun runLangRecognition(text: String): Flow<String> = callbackFlow {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                // if lang code == "und", language couldn't be resolved
                trySend(languageCode)
            }
            .addOnFailureListener {
                // Model couldn’t be loaded or other internal error.
                // ...
            }
        awaitClose { close() }
    }
}