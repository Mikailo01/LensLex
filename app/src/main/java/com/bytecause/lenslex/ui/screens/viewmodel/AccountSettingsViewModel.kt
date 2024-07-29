package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.ui.events.AccountSettingsUiEffect
import com.bytecause.lenslex.ui.events.AccountSettingsUiEvent
import com.bytecause.lenslex.ui.interfaces.AccountActionResult
import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.ui.screens.AccountSettingsMessage
import com.bytecause.lenslex.ui.screens.uistate.AccountSettingsState
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AccountSettingsViewModel(
    private val auth: FirebaseAuthClient,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val firebaseAuth = auth.getAuth()

    private val _uiState = MutableStateFlow(AccountSettingsState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AccountSettingsUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    // When the user deletes account this listener will be notified
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser == null) {
            _uiState.update { state ->
                state.copy(userDetails = null)
            }
        }
    }

    // Attach listener after viewmodel initialization
    init {
        fetchUserData()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun uiEventHandler(event: AccountSettingsUiEvent) {
        when (event) {
            AccountSettingsUiEvent.OnDeleteAccountButtonClick -> onDeleteAccountClick()
            AccountSettingsUiEvent.OnConfirmConfirmationDialog -> onConfirmConfirmationDialog()
            AccountSettingsUiEvent.OnDismissConfirmationDialog -> onDismissConfirmationDialog()
            AccountSettingsUiEvent.OnLaunchReauthenticationGoogleIntent -> sendEffect(
                AccountSettingsUiEffect.ReauthenticateWithGoogleProvider
            )

            AccountSettingsUiEvent.OnNavigateBack -> sendEffect(AccountSettingsUiEffect.NavigateBack)
            is AccountSettingsUiEvent.OnShowSnackBar -> sendEffect(
                AccountSettingsUiEffect.ShowMessage(
                    event.message
                )
            )

            is AccountSettingsUiEvent.OnReauthenticateWithGoogle -> reauthenticateWithGoogle(event.value)
            is AccountSettingsUiEvent.OnShowCredentialDialog -> onShowCredentialDialog(event.value)
            is AccountSettingsUiEvent.OnLinkButtonClick -> onLinkButtonClick(event.value)
            is AccountSettingsUiEvent.OnEnteredCredential -> onEnteredCredential(event.value)
            is AccountSettingsUiEvent.OnCredentialsDialogDismiss -> onCredentialsDialogDismiss()

            is AccountSettingsUiEvent.OnDialogCredentialChanged -> onDialogCredentialChanged(
                event.value
            )

            is AccountSettingsUiEvent.OnLinkGoogleProvider -> linkGoogleProvider(event.value)
        }
    }

    private fun fetchUserData() {
        val userDetails = userRepository.getUserData()
        val linkedProviders = userRepository.linkedProviders()
        _uiState.value =
            AccountSettingsState(userDetails = userDetails, linkedProviders = linkedProviders)
    }

    private fun sendEffect(effect: AccountSettingsUiEffect) {
        _effect.trySend(effect)
    }

    private fun onDeleteAccountClick() {
        _uiState.update {
            it.copy(showConfirmationDialog = true)
        }
    }

    private fun onConfirmConfirmationDialog() {
        deleteAccount()
        _uiState.update {
            it.copy(showConfirmationDialog = false)
        }
    }

    private fun onDismissConfirmationDialog() {
        _uiState.update {
            it.copy(showConfirmationDialog = false)
        }
    }

    private fun onShowCredentialDialog(credentialType: CredentialType) {
        _uiState.update {
            it.copy(
                showCredentialUpdateDialog = credentialType
            )
        }
    }

    private fun onLinkButtonClick(provider: Provider) {
        when (provider) {
            Provider.Email -> {
                if (_uiState.value.linkedProviders.contains(provider)) unlinkProvider(
                    provider
                )
                else _uiState.update { it.copy(showCredentialUpdateDialog = CredentialType.AccountLink) }
            }

            Provider.Google ->
                if (_uiState.value.linkedProviders.contains(provider)) unlinkProvider(
                    provider
                )
                else sendEffect(AccountSettingsUiEffect.LinkGoogleProvider)
        }
    }

    private fun onEnteredCredential(credentials: Credentials) {
        val validationResult = _uiState.value.credentialValidationResult

        when (_uiState.value.showCredentialUpdateDialog) {
            is CredentialType.Reauthorization -> {
                if (validationResult is CredentialValidationResult.Valid) {
                    val signInCredentials =
                        credentials as Credentials.Sensitive.SignInCredentials
                    reauthenticateUsingEmailAndPassword(signInCredentials)
                }
            }

            is CredentialType.AccountLink -> {
                if (validationResult is CredentialValidationResult.Valid) {
                    linkEmailProvider(
                        credentials as Credentials.Sensitive
                    )
                }
            }

            is CredentialType.Username -> {
                (credentials as Credentials.Insensitive.UsernameUpdate)
                    .takeIf { user -> user.username.isNotBlank() }
                    ?.let { user ->
                        updateUserName(user.username)
                    }
            }

            is CredentialType.Email -> {
                if (validationResult is CredentialValidationResult.Valid) {
                    updateEmail((credentials as Credentials.Sensitive.EmailCredential).email)
                }
            }

            is CredentialType.Password -> {
                if (validationResult is CredentialValidationResult.Valid) {
                    updatePassword((credentials as Credentials.Sensitive.PasswordCredential).password)
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    private fun onCredentialsDialogDismiss() {
        _uiState.update {
            it.copy(showCredentialUpdateDialog = null)
        }
    }

    private fun onDialogCredentialChanged(credentials: Credentials.Sensitive) {
        _uiState.update {
            it.copy(credentialValidationResult = ValidationUtil.areCredentialsValid(credentials))
        }
    }

    private fun reauthenticateWithGoogle(
        authCredential: AuthCredential
    ) {
        viewModelScope.launch {
            auth.reauthenticateWithGoogle(authCredential).firstOrNull()?.let { result ->
                result.onFailure { exception ->
                    sendEffect(
                        AccountSettingsUiEffect.AccountActionResult(
                            AccountActionResult.Failure.Error(exception)
                        )
                    )
                }
            }
        }
    }

    private fun unlinkProvider(provider: Provider) {
        viewModelScope.launch {
            when (provider) {
                Provider.Google -> {
                    userRepository.unlinkGoogleProvider().firstOrNull()?.let { result ->
                        result
                            .onSuccess {
                                _uiState.update {
                                    it.copy(linkedProviders = it.linkedProviders.filter { provider -> provider != Provider.Google })
                                }
                                reload()
                            }
                            .onFailure { exception ->
                                sendEffect(
                                    AccountSettingsUiEffect.AccountActionResult(
                                        AccountActionResult.Failure.Error(exception)
                                    )
                                )
                            }
                    }
                }

                Provider.Email -> {
                    userRepository.unlinkEmailProvider().firstOrNull()?.let { result ->
                        result
                            .onSuccess {
                                _uiState.update {
                                    it.copy(linkedProviders = it.linkedProviders.filter { it != Provider.Email })
                                }
                                reload()
                            }
                            .onFailure { exception ->
                                sendEffect(
                                    AccountSettingsUiEffect.AccountActionResult(
                                        AccountActionResult.Failure.Error(exception)
                                    )
                                )
                            }
                    }
                }
            }
        }
    }

    private fun linkGoogleProvider(authCredential: AuthCredential) {
        viewModelScope.launch {
            auth.linkGoogleProvider(authCredential).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(linkedProviders = it.linkedProviders + Provider.Google)
                        }
                        reload()
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            sendEffect(
                                AccountSettingsUiEffect.AccountActionResult(
                                    AccountActionResult.Failure.ReauthorizationRequired
                                )
                            )
                            return@let
                        }

                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    private fun linkEmailProvider(credentials: Credentials.Sensitive) {
        viewModelScope.launch {
            val credential = (credentials as Credentials.Sensitive.SignInCredentials)

            userRepository.linkEmailProvider(
                email = credential.email,
                password = credential.password
            ).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(linkedProviders = it.linkedProviders + Provider.Email)
                        }

                        reload()
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            sendEffect(
                                AccountSettingsUiEffect.AccountActionResult(
                                    AccountActionResult.Failure.ReauthorizationRequired
                                )
                            )
                            return@let
                        }

                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    // Manual refresh of firebase account details
    private fun reload() {
        userRepository.reloadUserData().let { newUserData ->
            _uiState.update {
                it.copy(userDetails = newUserData)
            }
        }

    }

    private fun updateUserName(userName: String) {
        viewModelScope.launch {
            userRepository.updateUsername(userName).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Success(AccountSettingsMessage.Username)
                            )
                        )

                        _uiState.update {
                            it.copy(userDetails = it.userDetails?.copy(userName = userName))
                        }
                    }
                    .onFailure { exception ->
                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    // If the user wants to make dangerous changes (i.e.: change email, password, delete account)
    // recent log in is required by firebase, so if exception is thrown by firebase, then this method
    // is called credentialChangeState value object holds state with new email or password, so after
    // reauthorization user doesn't have to type new credential change request, if state is empty,
    // then it means that user wants to delete his account.
    private fun reauthenticateUsingEmailAndPassword(
        credentials: Credentials.Sensitive.SignInCredentials
    ) {
        viewModelScope.launch {
            auth.reauthenticateWithEmailAndPassword(
                email = credentials.email,
                password = credentials.password
            ).firstOrNull()?.let { result ->
                result.onFailure { exception ->
                    sendEffect(
                        AccountSettingsUiEffect.AccountActionResult(
                            AccountActionResult.Failure.Error(exception)
                        )
                    )
                }
            }
        }
    }

    private fun updateEmail(email: String) {
        viewModelScope.launch {
            userRepository.updateEmail(email).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Success(AccountSettingsMessage.Email)
                            )
                        )

                        _uiState.update {
                            it.copy(userDetails = it.userDetails?.copy(email = email))
                        }
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            sendEffect(
                                AccountSettingsUiEffect.AccountActionResult(
                                    AccountActionResult.Failure.ReauthorizationRequired
                                )
                            )
                            return@let
                        }

                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    private fun updatePassword(password: String) {
        viewModelScope.launch {
            userRepository.updatePassword(password).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Success(AccountSettingsMessage.Password)
                            )
                        )
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            sendEffect(
                                AccountSettingsUiEffect.AccountActionResult(
                                    AccountActionResult.Failure.ReauthorizationRequired
                                )
                            )
                            return@let
                        }

                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    // User's data should be deleted automatically using Firebase CLI, but this app is not for
    // production use, so I used this workaround, which is not recommended for production-ready apps
    private fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteUserAccount().firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Success(AccountSettingsMessage.AccountDeletion)
                            )
                        )
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            sendEffect(
                                AccountSettingsUiEffect.AccountActionResult(
                                    AccountActionResult.Failure.ReauthorizationRequired
                                )
                            )
                            return@let
                        }

                        sendEffect(
                            AccountSettingsUiEffect.AccountActionResult(
                                AccountActionResult.Failure.Error(exception)
                            )
                        )
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}