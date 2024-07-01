package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.domain.models.UserAccountDetails
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FirebaseProviderId = "firebase"

class AccountSettingsViewModel(
    private val auth: Authenticator,
    private val firebase: FirebaseFirestore,
) : ViewModel() {

    private val firebaseAuth = auth.getAuth()

    private val _uiState =
        MutableStateFlow(
            AccountSettingsState(
                userDetails = auth.getAuth().currentUser?.run {
                    UserAccountDetails(
                        uid = uid,
                        creationTimeStamp = metadata?.creationTimestamp,
                        userName = displayName?.takeIf { it.isNotBlank() } ?: "",
                        email = email?.takeIf { it.isNotBlank() }
                            ?: providerData.find { it.email?.isNotBlank() == true }?.email ?: "",
                        isAnonymous = isAnonymous
                    )
                },
                linkedProviders = auth.getAuth().currentUser?.run {
                    val providers = mutableListOf<Provider>()

                    reload()

                    // Check for email
                    if (providerData.find { it.providerId == EmailAuthProvider.PROVIDER_ID } != null) {
                        providers.add(Provider.Email)
                    }

                    // Check for Google provider directly
                    if (providerData.find { it.providerId == GoogleAuthProvider.PROVIDER_ID } != null) {
                        providers.add(Provider.Google)
                    }

                    providers.toList()
                } ?: emptyList()
            )
        )
    val uiState = _uiState.asStateFlow()

    // Credential Manager needs context instance, so I created this StateFlow to notify the UI, that
    // intent should be launched
    private val _launchGoogleIntent = MutableStateFlow(false)
    val launchGoogleIntent = _launchGoogleIntent.asStateFlow()

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
        _launchGoogleIntent.value = boolean
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
                else _launchGoogleIntent.value = true
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
        val credentialChangeResult =
            _uiState.value.credentialChangeResult as CredentialChangeResult.Failure.ReauthorizationRequired

        viewModelScope.launch(Dispatchers.Main) {
            auth.getAuth().currentUser
                ?.reauthenticateAndRetrieveData(authCredential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
                    } else {
                        task.exception?.let {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.Error(it)
                            )
                        }
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
        when (provider) {
            Provider.Google -> {
                auth.getAuth().currentUser
                    ?.unlink(GoogleAuthProvider.PROVIDER_ID)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            _uiState.update {
                                it.copy(linkedProviders = it.linkedProviders.filter { provider -> provider != Provider.Google })
                            }

                            reload()
                        } else {
                            task.exception?.let {
                                updateCredentialChangeState(CredentialChangeResult.Failure.Error(it))
                            }
                        }
                    }
            }

            Provider.Email -> {
                auth.getAuth().currentUser
                    ?.unlink(EmailAuthProvider.PROVIDER_ID)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            _uiState.update {
                                it.copy(linkedProviders = it.linkedProviders.filter { it != Provider.Email })
                            }

                            reload()
                        } else {
                            task.exception?.let {
                                updateCredentialChangeState(CredentialChangeResult.Failure.Error(it))
                            }
                        }
                    }
            }
        }
    }

    fun linkGoogleProvider(authCredential: AuthCredential) {
        viewModelScope.launch {
            firebaseAuth.currentUser?.linkWithCredential(
                authCredential
            )?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    _uiState.update {
                        it.copy(linkedProviders = it.linkedProviders + Provider.Google)
                    }

                    resetCredentialChangeState()
                    reload()
                } else {
                    task.exception?.let {
                        if (it is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired()
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
                        )
                    }
                }
                _launchGoogleIntent.value = false
            }
        }
    }

    private fun linkEmailProvider(credentials: Credentials.Sensitive) {
        val credential = (credentials as Credentials.Sensitive.SignInCredentials)
        val emailCredentials =
            EmailAuthProvider.getCredential(credential.email, credential.password)
        auth.getAuth().currentUser?.linkWithCredential(emailCredentials)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    _uiState.update {
                        it.copy(linkedProviders = it.linkedProviders + Provider.Email)
                    }

                    resetCredentialChangeState()
                    reload()
                } else {
                    task.exception?.let {
                        if (it is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired()
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
                        )
                    }
                }
            }
    }

    // Manual refresh of firebase account details
    private fun reload() {
        auth.getAuth().currentUser?.reload()

        _uiState.update {
            it.copy(userDetails = auth.getAuth().currentUser?.run {
                UserAccountDetails(
                    uid = uid,
                    creationTimeStamp = metadata?.creationTimestamp,
                    userName = displayName?.takeIf { it.isNotBlank() },
                    email = providerData.find { it.providerId == FirebaseProviderId }?.email.takeIf { it?.isNotBlank() == true }
                        ?: providerData.find { it.email?.isNotBlank() == true }?.email,
                    isAnonymous = isAnonymous
                )
            })
        }
    }

    private fun updateUserName(userName: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        auth.getAuth().currentUser?.updateProfile(changeRequest)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCredentialChangeState(
                        CredentialChangeResult.Success(R.string.username_changed_message)
                    )

                    _uiState.update {
                        it.copy(userDetails = it.userDetails?.copy(userName = userName))
                    }
                } else {
                    task.exception?.let {
                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
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
        val credential = EmailAuthProvider.getCredential(credentials.email, credentials.password)
        auth.getAuth().currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (_uiState.value.credentialChangeResult is CredentialChangeResult.Failure.ReauthorizationRequired) {
                        val result =
                            (_uiState.value.credentialChangeResult as CredentialChangeResult.Failure.ReauthorizationRequired)

                        when {
                            result.email != null -> {
                                updateEmail(result.email)
                            }

                            result.password != null -> {
                                updatePassword(result.password)
                            }

                            result.deleteAccount -> deleteAccount()
                        }
                    }
                    resetCredentialChangeState()
                    reload()
                } else {
                    task.exception?.let {
                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
                        )
                    }
                }
            }
    }

    private fun updateEmail(email: String) {
        auth.getAuth().currentUser?.verifyBeforeUpdateEmail(email)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCredentialChangeState(
                        CredentialChangeResult.Success(R.string.email_changed_message)
                    )

                    _uiState.update {
                        it.copy(userDetails = it.userDetails?.copy(email = email))
                    }
                } else {
                    task.exception?.let {
                        if (it is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired(email = email)
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
                        )
                    }
                }
            }
    }

    private fun updatePassword(password: String) {
        auth.getAuth().currentUser?.updatePassword(password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCredentialChangeState(
                        CredentialChangeResult.Success(R.string.password_changed_message)
                    )
                } else {
                    task.exception?.let {
                        if (it is FirebaseAuthRecentLoginRequiredException) {
                            updateCredentialChangeState(
                                CredentialChangeResult.Failure.ReauthorizationRequired(password = password)
                            )
                            return@let
                        }

                        updateCredentialChangeState(
                            CredentialChangeResult.Failure.Error(it)
                        )
                    }
                }
            }
    }

    private fun deleteAccount() {
        val uid = auth.getAuth().currentUser?.uid
        uid?.let { userId ->
            viewModelScope.launch {
                firebase
                    .collection("users")
                    .document(userId)
                    .delete()
                    .addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            auth.getAuth().currentUser?.delete()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        updateCredentialChangeState(
                                            CredentialChangeResult.Success(R.string.account_deleted_message)
                                        )
                                    } else {
                                        task.exception?.let innerLet@{
                                            if (it is FirebaseAuthRecentLoginRequiredException) {
                                                updateCredentialChangeState(
                                                    CredentialChangeResult.Failure.ReauthorizationRequired(
                                                        deleteAccount = true
                                                    )
                                                )
                                                return@innerLet
                                            }

                                            updateCredentialChangeState(
                                                CredentialChangeResult.Failure.Error(it)
                                            )
                                        }
                                    }
                                }
                        }
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}