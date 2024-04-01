package com.bytecause.lenslex.mlkit

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class TextRecognizer(private val context: Context) {

    fun runTextRecognition(imagePaths: List<Uri>, callback: (List<String>) -> Unit) {
        val allRecognizedText = mutableListOf<String>()
        var imagesProcessed = 0

        for (imagePath in imagePaths) {
            val image = InputImage.fromFilePath(context, imagePath)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

            recognizer.process(image)
                .addOnSuccessListener { texts ->
                    if (texts.text.isNotBlank()) allRecognizedText.add(texts.text)
                    imagesProcessed++
                    if (imagesProcessed == imagePaths.size) {
                        callback(allRecognizedText)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
}