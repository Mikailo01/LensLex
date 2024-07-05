package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.ui.events.AccountSettingsUiEvent
import com.bytecause.lenslex.ui.interfaces.CredentialChangeResult
import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.ui.screens.uistate.AccountSettingsConfirmationDialog
import com.bytecause.lenslex.ui.screens.uistate.AccountSettingsState
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AccountSettingsViewModel(
    private val auth: FirebaseAuthClient,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val firebaseAuth = auth.getAuth()

    private val _uiState =
        MutableStateFlow(
            AccountSettingsState(
                userDetails = userRepository.getUserData(),
                linkedProviders = userRepository.linkedProviders()
            )
        )
    val uiState = _uiState.asStateFlow()

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
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun shouldLaunchGoogleIntent(boolean: Boolean) {
        _uiState.update {
            it.copy(launchGoogleIntent = boolean)
        }
    }

    fun uiEventHandler(event: AccountSettingsUiEvent.NonDirect) {
        when (event) {
            AccountSettingsUiEvent.OnDeleteAccountButtonClick -> onDeleteAccountClickHandler()
            AccountSettingsUiEvent.OnConfirmConfirmationDialog -> onConfirmConfirmationDialogHandler()
            AccountSettingsUiEvent.OnDismissConfirmationDialog -> onDismissConfirmationDialogHandler()
            is AccountSettingsUiEvent.OnShowCredentialDialog -> onShowCredentialDialogHandler(event.value)
            is AccountSettingsUiEvent.OnLinkButtonClick -> onLinkButtonClickHandler(event.value)
            is AccountSettingsUiEvent.OnEnteredCredential -> onEnteredCredentialHandler(event.value)
            is AccountSettingsUiEvent.OnCredentialsDialogDismiss -> onCredentialsDialogDismissHandler(
                event.value
            )

            is AccountSettingsUiEvent.OnDialogCredentialChanged -> onDialogCredentialChangedHandler(
                event.value
            )
        }
    }

    private fun onDeleteAccountClickHandler() {
        _uiState.update {
            it.copy(showConfirmationDialog = AccountSettingsConfirmationDialog.DeleteAccountWarning)
        }
    }

    private fun onConfirmConfirmationDialogHandler() {
        _uiState.value.showConfirmationDialog?.let { dialog ->
            when (dialog) {
                AccountSettingsConfirmationDialog.DeleteAccountWarning -> {
                    _uiState.update {
                        it.copy(showConfirmationDialog = null)
                    }
                    deleteAccount()
                }

                AccountSettingsConfirmationDialog.PasswordChangeWarning -> {
                    _uiState.update {
                        it.copy(showConfirmationDialog = null)
                    }
                }
            }
        }
    }

    private fun onDismissConfirmationDialogHandler() {
        _uiState.update {
            it.copy(showConfirmationDialog = null)
        }
    }

    private fun onShowCredentialDialogHandler(credentialType: CredentialType) {
        _uiState.update {
            it.copy(
                showCredentialUpdateDialog = credentialType,
                showConfirmationDialog = if (credentialType is CredentialType.Password) AccountSettingsConfirmationDialog.PasswordChangeWarning else it.showConfirmationDialog
            )
        }
    }

    private fun onLinkButtonClickHandler(provider: Provider) {
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
                else shouldLaunchGoogleIntent(true)
        }
    }

    private fun onEnteredCredentialHandler(credentials: Credentials) {
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
                _uiState.update { it.copy(showCredentialUpdateDialog = null) }
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

    private fun onCredentialsDialogDismissHandler(credentialType: CredentialType) {
        if (credentialType is CredentialType.Reauthorization) resetCredentialChangeState()
        _uiState.update {
            it.copy(showCredentialUpdateDialog = null)
        }
    }

    private fun onDialogCredentialChangedHandler(credentials: Credentials.Sensitive) {
        _uiState.update {
            it.copy(credentialValidationResult = ValidationUtil.areCredentialsValid(credentials))
        }
    }

    fun resetCredentialChangeState() {
        _uiState.update {
            it.copy(credentialChangeResult = null)
        }
    }

    private fun updateCredentialChangeState(credentialChangeResult: CredentialChangeResult) {
        _uiState.update {
            it.copy(credentialChangeResult = credentialChangeResult)
        }
    }

    fun reauthenticateWithGoogle(
        authCredential: AuthCredential
    ) {
        viewModelScope.launch {
            val credentialChangeResult =
                _uiState.value.credentialChangeResult as CredentialChangeResult.Failure.ReauthorizationRequired

            auth.reauthenticateWithGoogle(authCredential).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        when {
                            credentialChangeResult.email != null -> {
                                updateEmail(credentialChangeResult.email)
                            }

                            credentialChangeResult.password != null -> {
                                updatePassword(credentialChangeResult.password)
                            }

                            credentialChangeResult.deleteAccount -> deleteAccount()
                        }
                        reload()
                    }
                    .onFailure { exception ->
                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
                        )
                    }
            }
        }.invokeOnCompletion {
            resetCredentialChangeState()
            _uiState.update {
                it.copy(showCredentialUpdateDialog = null)
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
                                updateCredentialChangeState(
                                    CredentialChangeResult.Failure.Error(
                                        exception
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
                                updateCredentialChangeState(
                                    CredentialChangeResult.Failure.Error(
                                        exception
                                    )
                                )
                            }
                    }
                }
            }
        }
    }

    fun linkGoogleProvider(authCredential: AuthCredential) {
        viewModelScope.launch {
            auth.linkGoogleProvider(authCredential).firstOrNull()?.let { result ->
                result
                    .onSuccess {
                        _uiState.update {
                            it.copy(linkedProviders = it.linkedProviders + Provider.Google)
                        }

                        resetCredentialChangeState()
                        reload()
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired()
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
                        )
                    }
            }
        }.invokeOnCompletion { shouldLaunchGoogleIntent(false) }
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

                        resetCredentialChangeState()
                        reload()
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired()
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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
                        updateCredentialChangeState(
                            CredentialChangeResult.Success(R.string.username_changed_message)
                        )

                        _uiState.update {
                            it.copy(userDetails = it.userDetails?.copy(userName = userName))
                        }
                    }
                    .onFailure { exception ->
                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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
                result
                    .onSuccess {
                        if (_uiState.value.credentialChangeResult is CredentialChangeResult.Failure.ReauthorizationRequired) {
                            val credentialResult =
                                (_uiState.value.credentialChangeResult as CredentialChangeResult.Failure.ReauthorizationRequired)

                            when {
                                credentialResult.email != null -> {
                                    updateEmail(credentialResult.email)
                                }

                                credentialResult.password != null -> {
                                    updatePassword(credentialResult.password)
                                }

                                credentialResult.deleteAccount -> deleteAccount()
                            }
                        }
                        resetCredentialChangeState()
                        reload()
                    }
                    .onFailure { exception ->
                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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
                        updateCredentialChangeState(
                            CredentialChangeResult.Success(R.string.email_changed_message)
                        )

                        _uiState.update {
                            it.copy(userDetails = it.userDetails?.copy(email = email))
                        }
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired(email = email)
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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
                        updateCredentialChangeState(
                            CredentialChangeResult.Success(R.string.password_changed_message)
                        )
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired(password = password)
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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
                        updateCredentialChangeState(
                            CredentialChangeResult.Success(R.string.account_deleted_message)
                        )
                    }
                    .onFailure { exception ->
                        if (exception is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired(
                                    deleteAccount = true
                                )
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(exception)
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