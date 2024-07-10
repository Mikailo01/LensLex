package com.bytecause.lenslex.data.local.mlkit

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TranslationModelManager {

    private val modelManager = RemoteModelManager.getInstance()

    fun getModels(): Flow<Result<Set<TranslateRemoteModel>>> = callbackFlow {
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                trySend(Result.success(models))
            }
            .addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    fun deleteModel(languageTag: String): Flow<Result<Unit>> = callbackFlow {
        TranslateLanguage.fromLanguageTag(languageTag)?.let { langTag ->
            val model = TranslateRemoteModel.Builder(langTag).build()
            modelManager.deleteDownloadedModel(model)
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    trySend(Result.failure(exception))
                }
        }
        awaitClose()
    }

    fun downloadModel(languageTag: String): Flow<Result<Unit>> = callbackFlow {
        TranslateLanguage.fromLanguageTag(languageTag)?.let { langTag ->
            val model = TranslateRemoteModel.Builder(langTag).build()
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    trySend(Result.failure(exception))
                }
        }
        awaitClose()
    }
}