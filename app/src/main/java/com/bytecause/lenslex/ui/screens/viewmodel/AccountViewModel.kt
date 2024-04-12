package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.UserData
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AccountViewModel(
    private val fireBaseAuthClient: FireBaseAuthClient
) : ViewModel() {

    val isAccountAnonymous: Boolean = fireBaseAuthClient.getSignedInUser()?.isAnonymous == true

    val getSignedInUser = MutableStateFlow(fireBaseAuthClient.getSignedInUser()?.run {
        Log.d("idk", "user")
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

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

    override fun onCleared() {
        super.onCleared()
        Log.d("idk", "cleared")
    }
}