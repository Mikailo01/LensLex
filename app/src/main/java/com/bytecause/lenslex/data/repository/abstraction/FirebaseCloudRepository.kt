package com.bytecause.lenslex.data.repository.abstraction

import android.net.Uri

interface FirebaseCloudRepository {
    suspend fun saveUserProfilePicture(imageUri: Uri)
}