package com.bytecause.lenslex.mlkit

import android.util.Log
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
        // Create an English-German translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val czechEnglishTranslator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        czechEnglishTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // Model downloaded successfully. Okay to start translating.
                // (Set a flag, unhide the translation UI, etc.)
                czechEnglishTranslator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        // Translation successful.
                        onTranslateResult(TranslationResult.TranslationSuccess(translatedText = translatedText))
                       // onTranslateResult(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        onTranslateResult(TranslationResult.TranslationFailure)
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("idk", "donm't")
                onTranslateResult(TranslationResult.ModelDownloadFailure)
                // Model couldn’t be downloaded or other internal error.
                // ...
            }


    }

}