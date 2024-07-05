package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.FirebaseCloudRepository
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.domain.models.UserData
import com.bytecause.lenslex.ui.events.AccountUiEvent
import com.bytecause.lenslex.ui.screens.uistate.AccountState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val auth: FirebaseAuthClient,
    private val firebaseCloudRepository: FirebaseCloudRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val firebaseAuth = auth.getAuth()

    private val _uiState =
        MutableStateFlow(AccountState(userData = userRepository.getUserData()?.run {
            UserData(
                userId = uid,
                userName = userName,
                profilePictureUrl = profilePictureUrl,
                isAnonymous = isAnonymous
            )
        }
        )
        )
    val uiState = _uiState.asStateFlow()

    fun uiEventHandler(event: AccountUiEvent.NonDirect) {
        when (event) {
            is AccountUiEvent.OnUpdateName -> onUpdateNameHandler(event.value)
            is AccountUiEvent.OnUpdateProfilePicture -> onUpdateProfilePictureHandler(event.value)
            AccountUiEvent.OnEditChange -> onEditChangeHandler()
            is AccountUiEvent.OnChangeFirebaseLanguage -> changeFirebaseLanguageCode(event.value)
            is AccountUiEvent.OnShowConfirmationDialog -> onShowConfirmationDialogHandler(event.value)
            is AccountUiEvent.OnShowLanguageDialog -> onShowLanguageDialogHandler(event.value)
            is AccountUiEvent.OnShowBottomSheet -> onShowBottomSheetHandler(event.value)
            is AccountUiEvent.OnShowUrlDialog -> onShowUrlDialogHandler(event.value)
            AccountUiEvent.OnSignOut -> onSignOutHandler()
            is AccountUiEvent.OnNameTextFieldValueChange -> onNameTextFieldValueChangeHandler(event.value)
            is AccountUiEvent.OnUrlTextFieldValueChange -> onUrlTextFieldValueChange(event.value)
            is AccountUiEvent.OnSaveUserProfilePicture -> onSaveUserProfilePictureHandler(event.value)
        }
    }

    private fun onUpdateNameHandler(userName: String) {
        _uiState.update {
            it.copy(userData = it.userData?.copy(userName = userName))
        }
        updateName(userName)
    }

    private fun onUpdateProfilePictureHandler(profilePictureUrl: String) {
        _uiState.update {
            it.copy(userData = it.userData?.copy(profilePictureUrl = profilePictureUrl))
        }
        updateProfilePicture(Uri.parse(profilePictureUrl))
    }

    private fun onEditChangeHandler() {
        _uiState.update {
            it.copy(isEditing = !it.isEditing)
        }
    }

    private fun onShowConfirmationDialogHandler(boolean: Boolean) {
        _uiState.update {
            it.copy(showConfirmationDialog = boolean)
        }
    }

    private fun onShowLanguageDialogHandler(boolean: Boolean) {
        _uiState.update {
            it.copy(showLanguageDialog = boolean)
        }
    }

    private fun onShowBottomSheetHandler(boolean: Boolean) {
        _uiState.update {
            it.copy(showBottomSheet = boolean)
        }
    }

    private fun onShowUrlDialogHandler(boolean: Boolean) {
        _uiState.update {
            it.copy(showUrlDialog = boolean)
        }
    }

    private fun onSignOutHandler() {
        signOut()
        _uiState.update {
            it.copy(signedOutSuccess = true)
        }
    }

    private fun onNameTextFieldValueChangeHandler(userName: String) {
        _uiState.update {
            it.copy(userData = it.userData?.copy(userName = userName))
        }
    }

    private fun onUrlTextFieldValueChange(url: String) {
        _uiState.update {
            it.copy(urlValue = url)
        }
    }

    private fun onSaveUserProfilePictureHandler(imageUri: Uri) {
        viewModelScope.launch {
            firebaseCloudRepository.saveUserProfilePicture(imageUri)
        }
    }

    fun reload() {
        userRepository.reloadUserData()?.run {
            _uiState.update {
                it.copy(userData =
                userRepository.getUserData()?.run {
                    UserData(
                        userId = uid,
                        userName = userName,
                        profilePictureUrl = profilePictureUrl,
                        isAnonymous = isAnonymous
                    )
                }
                )
            }
        }
    }

    private val idTokenListener = FirebaseAuth.IdTokenListener {
        if (it.currentUser == null) _uiState.update { state -> state.copy(userData = null) }
    }

    private fun changeFirebaseLanguageCode(langCode: String) =
        firebaseAuth.setLanguageCode(langCode)

    init {
        firebaseAuth.addIdTokenListener(idTokenListener)
    }

    private fun updateName(name: String) {
        userRepository.updateUsername(name)
    }

    private fun updateProfilePicture(uri: Uri) {
        userRepository.updateProfilePicture(uri)
    }

    private fun signOut() = auth.signOut()

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeIdTokenListener(idTokenListener)
    }
}