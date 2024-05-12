package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.FirebaseCloudRepositoryImpl
import com.bytecause.lenslex.domain.models.UserData
import com.bytecause.lenslex.ui.screens.uistate.AccountState
import com.bytecause.lenslex.ui.events.AccountUiEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccountViewModel(
    private val auth: Authenticator,
    private val firebaseCloudRepository: FirebaseCloudRepositoryImpl
) : ViewModel() {

    private val firebaseAuth = auth.getAuth

    private val _uiState = MutableStateFlow(AccountState(userData = firebaseAuth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl.toString(),
            isAnonymous = isAnonymous
        )
    }
    )
    )
    val uiState = _uiState.asStateFlow()

    fun uiEventHandler(event: AccountUiEvent.NonDirect) {
        when (event) {
            is AccountUiEvent.OnUpdateName -> {
                _uiState.update {
                    it.copy(userData = it.userData?.copy(userName = event.value))
                }
                updateName(event.value)
            }

            is AccountUiEvent.OnUpdateProfilePicture -> {
                _uiState.update {
                    it.copy(userData = it.userData?.copy(profilePictureUrl = event.value))
                }
                updateProfilePicture(Uri.parse(event.value))
            }

            AccountUiEvent.OnEditChange -> {
                _uiState.update {
                    it.copy(isEditing = !it.isEditing)
                }
            }

            is AccountUiEvent.OnChangeFirebaseLanguage -> {
                changeFirebaseLanguageCode(event.value)
            }

            is AccountUiEvent.OnShowConfirmationDialog -> {
                _uiState.update {
                    it.copy(showConfirmationDialog = event.value)
                }
            }

            is AccountUiEvent.OnShowLanguageDialog -> {
                _uiState.update {
                    it.copy(showLanguageDialog = event.value)
                }
            }

            is AccountUiEvent.OnShowBottomSheet -> {
                _uiState.update {
                    it.copy(showBottomSheet = event.value)
                }
            }

            is AccountUiEvent.OnShowUrlDialog -> {
                _uiState.update {
                    it.copy(showUrlDialog = event.value)
                }
            }

            AccountUiEvent.OnSignOut -> {
                signOut()
                _uiState.update {
                    it.copy(signedOutSuccess = true)
                }
            }

            is AccountUiEvent.OnNameTextFieldValueChange -> {
                _uiState.update {
                    it.copy(userData = it.userData?.copy(userName = event.value))
                }
            }

            is AccountUiEvent.OnUrlTextFieldValueChange -> {
                _uiState.update {
                    it.copy(urlValue = event.value)
                }
            }

            is AccountUiEvent.OnSaveUserProfilePicture -> {
                firebaseCloudRepository.saveUserProfilePicture(event.value)
            }
        }
    }

    fun reload() {
        firebaseAuth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.update {
                    it.copy(userData =
                    firebaseAuth.currentUser?.run {
                        UserData(
                            userId = uid,
                            userName = displayName,
                            profilePictureUrl = photoUrl?.toString(),
                            isAnonymous = isAnonymous
                        )
                    }
                    )
                }
            }
        }
    }

    private val authStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) _uiState.update { state -> state.copy(userData = null) }
    }

    private fun changeFirebaseLanguageCode(langCode: String) =
        auth.getAuth.setLanguageCode(langCode)

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    private fun updateName(name: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.getAuth.currentUser?.updateProfile(changeRequest)
    }

    private fun updateProfilePicture(uri: Uri) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        auth.getAuth.currentUser?.updateProfile(changeRequest)
    }

    private fun signOut() = auth.signOut()
}