package com.bytecause.lenslex.data.repository

import android.net.Uri
import com.bytecause.lenslex.data.remote.FirebaseCloudStorage
import com.bytecause.lenslex.data.repository.abstraction.FirebaseCloudRepository

class FirebaseCloudRepositoryImpl(
    private val firebaseCloud: FirebaseCloudStorage
): FirebaseCloudRepository {
    override fun saveUserProfilePicture(imageUri: Uri) {
        firebaseCloud.saveUserProfilePicture(imageUri)
    }
}