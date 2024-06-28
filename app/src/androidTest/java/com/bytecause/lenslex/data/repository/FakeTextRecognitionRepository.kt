package com.bytecause.lenslex.data.repository

import android.net.Uri
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import kotlinx.coroutines.flow.Flow

class FakeTextRecognitionRepository: TextRecognitionRepository {
    override fun runTextRecognition(imagePaths: List<Uri>): Flow<List<String>> {
        TODO("Not yet implemented")
    }
}