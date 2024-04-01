package com.bytecause.lenslex.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

object Translator {

    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        onTranslateResult: (String) -> Unit
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
                        onTranslateResult(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        // Error.
                        // ...
                    }
            }
            .addOnFailureListener { exception ->
                // Model couldn’t be downloaded or other internal error.
                // ...
            }


    }

}