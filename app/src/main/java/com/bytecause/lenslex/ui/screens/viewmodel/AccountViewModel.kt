package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.FirebaseCloudRepository
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.domain.models.UserData
import com.bytecause.lenslex.ui.events.AccountUiEffect
import com.bytecause.lenslex.ui.events.AccountUiEvent
import com.bytecause.lenslex.ui.screens.model.AccountState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val auth: FirebaseAuthClient,
    private val firebaseCloudRepository: FirebaseCloudRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val firebaseAuth = auth.getAuth()

    private val _uiState =
        MutableStateFlow(AccountState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AccountUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    fun uiEventHandler(event: AccountUiEvent) {
        when (event) {
            is AccountUiEvent.OnUpdateProfilePicture -> onUpdateProfilePicture(event.value)
            is AccountUiEvent.OnChangeFirebaseLanguage -> changeFirebaseLanguageCode(event.value)
            is AccountUiEvent.OnShowConfirmationDialog -> onShowConfirmationDialog(event.value)
            is AccountUiEvent.OnShowLanguageDialog -> onShowLanguageDialog(event.value)
            is AccountUiEvent.OnShowBottomSheet -> onShowBottomSheet(event.value)
            is AccountUiEvent.OnShowUrlDialog -> onShowUrlDialog(event.value)
            is AccountUiEvent.OnNameTextFieldValueChange -> onNameTextFieldValueChange(event.value)
            is AccountUiEvent.OnUrlTextFieldValueChange -> onUrlTextFieldValueChange(event.value)
            is AccountUiEvent.OnSaveUserProfilePicture -> onSaveUserProfilePicture(event.value)
            is AccountUiEvent.OnImageLoading -> onImageLoading(event.value)
            is AccountUiEvent.OnNavigate -> sendEffect(AccountUiEffect.NavigateTo(event.destination))
            AccountUiEvent.OnSignOut -> onSignOut()
            AccountUiEvent.OnGetUserData -> getUserData()
            AccountUiEvent.OnBackButtonClick -> sendEffect(AccountUiEffect.NavigateBack)
            AccountUiEvent.OnSinglePicturePickerLaunch -> sendEffect(AccountUiEffect.SinglePicturePickerLaunch)
        }
    }

    private fun sendEffect(effect: AccountUiEffect) {
        _effect.trySend(effect)
    }

    private fun getUserData() {
        val userData = userRepository.getUserData()?.run {
            UserData(
                userId = uid,
                userName = userName,
                profilePictureUrl = profilePictureUrl,
                isAnonymous = isAnonymous
            )
        }

        _uiState.update {
            it.copy(userData = userData)
        }
    }

    private fun onImageLoading(boolean: Boolean) {
        _uiState.update {
            it.copy(isImageLoading = boolean)
        }
    }

    private fun onUpdateProfilePicture(profilePictureUrl: String) {
        _uiState.update {
            it.copy(userData = it.userData?.copy(profilePictureUrl = profilePictureUrl))
        }
        updateProfilePicture(Uri.parse(profilePictureUrl))
    }

    private fun onShowConfirmationDialog(boolean: Boolean) {
        _uiState.update {
            it.copy(showConfirmationDialog = boolean)
        }
    }

    private fun onShowLanguageDialog(boolean: Boolean) {
        _uiState.update {
            it.copy(showLanguageDialog = boolean)
        }
    }

    private fun onShowBottomSheet(boolean: Boolean) {
        _uiState.update {
            it.copy(showBottomSheet = boolean)
        }
    }

    private fun onShowUrlDialog(boolean: Boolean) {
        _uiState.update {
            it.copy(showUrlDialog = boolean)
        }
    }

    private fun onSignOut() = signOut()

    private fun onNameTextFieldValueChange(userName: String) {
        _uiState.update {
            it.copy(userData = it.userData?.copy(userName = userName))
        }
    }

    private fun onUrlTextFieldValueChange(url: String) {
        _uiState.update {
            it.copy(urlValue = url)
        }
    }

    private fun onSaveUserProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            firebaseCloudRepository.saveUserProfilePicture(imageUri)
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

    private fun updateProfilePicture(uri: Uri) {
        userRepository.updateProfilePicture(uri)
    }

    private fun signOut() = auth.signOut()

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeIdTokenListener(idTokenListener)
    }
}