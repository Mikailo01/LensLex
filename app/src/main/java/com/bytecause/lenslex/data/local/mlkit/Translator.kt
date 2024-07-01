package com.bytecause.lenslex.data.local.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Translator {

    sealed class TranslationResult {
        data class TranslationSuccess(val translatedText: String) : TranslationResult()
        data object ModelDownloadFailure : TranslationResult()
        data object TranslationFailure : TranslationResult()
    }

    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
    ): Flow<TranslationResult> = callbackFlow {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener {
            // Download successful, translate text
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    trySend(TranslationResult.TranslationSuccess(translatedText = translatedText))
                }
                .addOnFailureListener {
                    trySend(TranslationResult.TranslationFailure)
                }
        }
            .addOnFailureListener {
                // Download failed
                trySend(TranslationResult.ModelDownloadFailure)
            }

        awaitClose {
            translator.close()
        }
    }
}