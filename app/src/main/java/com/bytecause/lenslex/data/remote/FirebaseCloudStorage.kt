package com.bytecause.lenslex.data.remote

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class FirebaseCloudStorage {

    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private val userId = Firebase.auth.currentUser?.uid

    fun saveUserProfilePicture(imageUri: Uri) {
        val imageRef = storageRef.child("profile_pictures/${userId}")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                // Store the image URL in Firestore
                setPhotoUri(downloadUri)
               // storeImageUrl(downloadUri.toString())
            } else {
                // Handle failures
            }
        }
    }

    private fun setPhotoUri(imageUri: Uri) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(imageUri)
            .build()

        Firebase.auth.currentUser?.updateProfile(changeRequest)
    }
}