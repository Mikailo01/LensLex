package com.bytecause.lenslex.data.repository

import android.net.Uri
import com.bytecause.lenslex.data.remote.FirebaseCloudStorage
import com.bytecause.lenslex.data.repository.abstraction.FirebaseCloudRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FirebaseCloudRepositoryImpl(
    private val firebaseCloud: FirebaseCloudStorage,
    private val coroutineDispatcher: CoroutineDispatcher
) : FirebaseCloudRepository {
    override suspend fun saveUserProfilePicture(imageUri: Uri) = withContext(coroutineDispatcher) {
        firebaseCloud.saveUserProfilePicture(imageUri)
    }
}