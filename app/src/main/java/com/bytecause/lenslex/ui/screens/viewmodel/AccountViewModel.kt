package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccountViewModel(
    private val auth: AuthRepository
) : ViewModel() {

    private val firebaseAuth = auth.getFirebaseAuth

    val isAccountAnonymous: Boolean = firebaseAuth.currentUser?.isAnonymous == true

    private val _getSignedInUser = MutableStateFlow(firebaseAuth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }
    )

    fun reload() {
        firebaseAuth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _getSignedInUser.update {
                    firebaseAuth.currentUser?.run {
                        UserData(
                            userId = uid,
                            userName = displayName,
                            profilePictureUrl = photoUrl?.toString()
                        )
                    }
                }
            }
        }
    }

    val getSignedInUser: StateFlow<UserData?> = _getSignedInUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) _getSignedInUser.value = null
    }

    fun changeFirebaseLanguageCode(langCode: String) =
        auth.getFirebaseAuth.setLanguageCode(langCode)

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun updateName(name: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.getFirebaseAuth.currentUser?.updateProfile(changeRequest)
    }

    fun updateProfilePicture(uri: Uri) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        auth.getFirebaseAuth.currentUser?.updateProfile(changeRequest)
    }

    fun signOut() = auth.signOut()
}