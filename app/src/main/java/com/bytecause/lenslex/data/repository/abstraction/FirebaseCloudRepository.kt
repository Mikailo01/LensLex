package com.bytecause.lenslex.data.repository.abstraction

import android.net.Uri

interface FirebaseCloudRepository {

    fun saveUserProfilePicture(imageUri: Uri)
}