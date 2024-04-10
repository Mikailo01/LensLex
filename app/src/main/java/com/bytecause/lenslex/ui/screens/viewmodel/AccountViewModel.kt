package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.UserData
import com.google.firebase.auth.UserProfileChangeRequest

class AccountViewModel(
    private val fireBaseAuthClient: FireBaseAuthClient
) : ViewModel() {

    val isAccountAnonymous: Boolean = fireBaseAuthClient.getSignedInUser()?.isAnonymous == true

    val getSignedInUser: UserData? = fireBaseAuthClient.getSignedInUser()?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString().also { Log.d("idk2", it.toString()) }
        )
    }

    fun updateName(name: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        fireBaseAuthClient.getSignedInUser()?.updateProfile(changeRequest)
    }

    fun updateProfilePicture(uri: Uri) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        fireBaseAuthClient.getSignedInUser()?.updateProfile(changeRequest)
    }

    suspend fun signOut(): Boolean = fireBaseAuthClient.signOut()

    /*fun linkAnonymousUser(email: String, password: String) {
       fireBaseAuthClient.getSignedInUser()?.linkWithCredential()
   }*/
}