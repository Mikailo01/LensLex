package com.bytecause.lenslex.data.repository.abstraction

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface TextRecognitionRepository {
    fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>>
}