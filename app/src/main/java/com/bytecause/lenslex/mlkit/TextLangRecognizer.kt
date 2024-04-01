package com.bytecause.lenslex.mlkit

import android.content.res.Resources.NotFoundException
import com.google.mlkit.nl.languageid.LanguageIdentification

object TextLangRecognizer {

    fun identifyLanguage(
        text: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    onFailure(NotFoundException())
                } else {
                    onSuccess(languageCode)
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}