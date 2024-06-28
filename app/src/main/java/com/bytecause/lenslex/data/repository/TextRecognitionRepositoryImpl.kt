package com.bytecause.lenslex.data.repository

import android.net.Uri
import com.bytecause.lenslex.data.local.mlkit.TextRecognizer
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import kotlinx.coroutines.flow.Flow

class TextRecognitionRepositoryImpl(
    private val textRecognizer: TextRecognizer
) : TextRecognitionRepository {
    override fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> =
        textRecognizer.runTextRecognition(imagePaths)
}