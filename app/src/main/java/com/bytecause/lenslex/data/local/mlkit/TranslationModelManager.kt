package com.bytecause.lenslex.data.local.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import java.lang.Exception

class TranslationModelManager {

    private val modelManager = RemoteModelManager.getInstance()

    fun getModels(onResult: (Set<TranslateRemoteModel>) -> Unit) {
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                onResult(models)
            }
            .addOnFailureListener {
                // Error.
            }
    }

    fun deleteModel(languageTag: String, onRemoveSuccess: () -> Unit) {
        TranslateLanguage.fromLanguageTag(languageTag)?.let { langTag ->
            val model = TranslateRemoteModel.Builder(langTag).build()
            modelManager.deleteDownloadedModel(model)
                .addOnSuccessListener {
                    onRemoveSuccess()
                }
                .addOnFailureListener {
                    // Error.
                }
        }
    }

    fun downloadModel(languageTag: String, onDownloadSuccess: () -> Unit, onDownloadFailure: (Exception) -> Unit) {
        TranslateLanguage.fromLanguageTag(languageTag)?.let { langTag ->
            val model = TranslateRemoteModel.Builder(langTag).build()
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                    onDownloadSuccess()
                }
                .addOnFailureListener {
                    onDownloadFailure(it)
                }
        }
    }
}