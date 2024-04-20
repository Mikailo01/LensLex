package com.bytecause.lenslex.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object Translator {

    sealed class TranslationResult {
        data class TranslationSuccess(val translatedText: String) : TranslationResult()
        data object ModelDownloadFailure : TranslationResult()
        data object TranslationFailure : TranslationResult()
    }

    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        onTranslateResult: (TranslationResult) -> Unit
    ) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                translator.translate(text).addOnCompleteListener { translateTask ->
                    if (translateTask.isSuccessful) {
                        onTranslateResult(TranslationResult.TranslationSuccess(translatedText = translateTask.result))
                    } else {
                        onTranslateResult(TranslationResult.TranslationFailure)
                    }
                }
            } else {
                onTranslateResult(TranslationResult.ModelDownloadFailure)
            }
        }
    }

}