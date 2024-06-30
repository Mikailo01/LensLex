package com.bytecause.lenslex.data.local.mlkit

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class TextRecognizer(private val applicationContext: Context) {

    fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> = callbackFlow {
        val allRecognizedText = mutableListOf<String>()
        var imagesProcessed = 0

        for (imagePath in imagePaths) {
            val image = InputImage.fromFilePath(applicationContext, imagePath)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { texts ->
                    if (texts.text.isNotBlank()) allRecognizedText.add(texts.text)

                    imagesProcessed++

                    if (imagesProcessed == imagePaths.size) {
                        trySend(allRecognizedText)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Log.e("TextRecognition", e.message.toString())
                }
        }
        awaitClose { close() }
    }
}