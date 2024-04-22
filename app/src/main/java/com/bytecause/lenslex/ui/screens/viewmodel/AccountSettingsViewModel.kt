package com.bytecause.lenslex.ui.screens.viewmodel

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.R
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.util.CredentialValidationResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountSettingsViewModel(
    private val fireBaseAuthClient: FireBaseAuthClient,
    private val firebase: FirebaseFirestore,
) : ViewModel() {

    private val firebaseAuth = fireBaseAuthClient.getFirebaseAuth

    private val _credentialChangeState = MutableStateFlow<CredentialChangeResult?>(null)
    val credentialChangeState: StateFlow<CredentialChangeResult?> =
        _credentialChangeState.asStateFlow()

    private val _showCredentialUpdateDialog = MutableStateFlow<CredentialType?>(null)
    val showCredentialUpdateDialog = _showCredentialUpdateDialog.asStateFlow()

    private val _credentialValidationResultState =
        MutableStateFlow<CredentialValidationResult?>(null)
    val credentialValidationResultState: StateFlow<CredentialValidationResult?> =
        _credentialValidationResultState.asStateFlow()


    // When the user deletes account this listener will be notified
    private val authStateListener = FirebaseAuth.AuthStateListener {
        if (it.currentUser == null) {
            _getUserAccountDetails.value = null
        }
    }

    // Attach listener after viewmodel initialization
    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    private val _getUserAccountDetails =
        MutableStateFlow(fireBaseAuthClient.getFirebaseAuth.currentUser?.run {
            UserAccountDetails(
                uid = uid,
                creationTimeStamp = metadata?.creationTimestamp,
                userName = displayName?.takeIf { it.isNotBlank() } ?: "Not set",
                email = email?.takeIf { it.isNotBlank() }
                    ?: providerData.find { it.email?.isNotBlank() == true }?.email ?: "Not set",
                isAnonymous = isAnonymous
            )
        }
        )

    val getUserAccountDetails: StateFlow<UserAccountDetails?> =
        _getUserAccountDetails.asStateFlow()

    // List of supported providers which can be linked
    private val _getProviders = MutableStateFlow(
        fireBaseAuthClient.getFirebaseAuth.currentUser?.let { user ->
            val providers = mutableListOf<Provider>()

            user.reload()

            // Check for email
            if (user.providerData.find { it.providerId == EmailAuthProvider.PROVIDER_ID } != null) {
                providers.add(Provider.Email)
            }

            // Check for Google provider directly
            if (user.providerData.find { it.providerId == GoogleAuthProvider.PROVIDER_ID } != null) {
                providers.add(Provider.Google)
            }

            providers.toList()
        } ?: listOf()
    )
    val getProviders: StateFlow<List<Provider>?> = _getProviders.asStateFlow()

    fun saveCredentialValidationResult(result: CredentialValidationResult) {
        _credentialValidationResultState.update {
            result
        }
    }

    fun resetCredentialChangeState() {
        _credentialChangeState.value = null
    }

    private fun updateCredentialChangeState(credentialChangeResult: CredentialChangeResult) {
        _credentialChangeState.update {
            credentialChangeResult
        }
    }

    fun reauthenticateWithGoogle(
        context: Context,
        credentialChangeResult: CredentialChangeResult.Failure.ReauthorizationRequired
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            fireBaseAuthClient.getFirebaseAuth.currentUser
                ?.reauthenticateAndRetrieveData(fireBaseAuthClient.getGoogleCredential(context))
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        when {
                            credentialChangeResult.email != null -> {
                                updateEmail(credentialChangeResult.email)
                            }

                            credentialChangeResult.password != null -> {
                                updatePassword(credentialChangeResult.password)
                            }

                            else -> deleteAccount()
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
        }
    }

    fun unlinkProvider(provider: Provider) {
        when (provider) {
            Provider.Google -> {
                fireBaseAuthClient.getFirebaseAuth.currentUser
                    ?.unlink(GoogleAuthProvider.PROVIDER_ID)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _getProviders.value =
                                _getProviders.value.filter { it != Provider.Google }
                            reload()
                        } else {
                            task.exception?.let {
                                updateCredentialChangeState(CredentialChangeResult.Failure.Error(it))
                            }
                        }
                    }
            }

            Provider.Email -> {
                fireBaseAuthClient.getFirebaseAuth.currentUser
                    ?.unlink(EmailAuthProvider.PROVIDER_ID)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _getProviders.value =
                                _getProviders.value.filter { it != Provider.Email }
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

    fun linkGoogleProvider(context: Context) {
        viewModelScope.launch {
            firebaseAuth.currentUser?.linkWithCredential(
                fireBaseAuthClient.getGoogleCredential(
                    context
                )
            )?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _getProviders.value += Provider.Google

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
    }

    fun linkEmailProvider(credentials: Credentials.Sensitive) {
        val credential = (credentials as Credentials.Sensitive.SignInCredentials)
        val emailCredentials =
            EmailAuthProvider.getCredential(credential.email, credential.password)
        fireBaseAuthClient.getFirebaseAuth.currentUser?.linkWithCredential(emailCredentials)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _getProviders.value += Provider.Email

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

    fun showCredentialUpdateDialog(credentialType: CredentialType?) {
        _showCredentialUpdateDialog.value = credentialType
    }

    // Manual refresh of firebase account details
    private fun reload() {
        fireBaseAuthClient.getFirebaseAuth.currentUser?.reload()

        _getUserAccountDetails.value = fireBaseAuthClient.getFirebaseAuth.currentUser?.run {
            UserAccountDetails(
                uid = uid,
                creationTimeStamp = metadata?.creationTimestamp,
                userName = displayName?.takeIf { it.isNotBlank() } ?: "Not set",
                email = providerData.find { it.providerId == "firebase" }?.email.takeIf { it?.isNotBlank() == true }
                    ?: providerData.find { it.email?.isNotBlank() == true }?.email ?: "Not set",
                isAnonymous = isAnonymous
            )
        }
    }

    fun updateUserName(userName: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        fireBaseAuthClient.getFirebaseAuth.currentUser?.updateProfile(changeRequest)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCredentialChangeState(
                        CredentialChangeResult.Success(R.string.username_changed_message)
                    )

                    _getUserAccountDetails.value =
                        _getUserAccountDetails.value?.copy(userName = userName)
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
    fun reauthenticateUsingEmailAndPassword(credentials: Credentials.Sensitive.SignInCredentials) {
        val credential = EmailAuthProvider.getCredential(credentials.email, credentials.password)
        fireBaseAuthClient.getFirebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (credentialChangeState.value is CredentialChangeResult.Failure.ReauthorizationRequired) {
                        val savedFailureState =
                            (credentialChangeState.value as CredentialChangeResult.Failure.ReauthorizationRequired)
                        val newEmail = savedFailureState.email
                        val newPassword = savedFailureState.password

                        when {
                            newEmail != null -> {
                                updateEmail(newEmail)
                            }

                            newPassword != null -> {
                                updatePassword(newPassword)
                            }

                            else -> deleteAccount()
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

    fun updateEmail(email: String) {
        fireBaseAuthClient.getFirebaseAuth.currentUser?.verifyBeforeUpdateEmail(email)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateCredentialChangeState(
                        CredentialChangeResult.Success(R.string.email_changed_message)
                    )
                    _getUserAccountDetails.value =
                        _getUserAccountDetails.value?.copy(email = email)
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

    fun updatePassword(password: String) {
        fireBaseAuthClient.getFirebaseAuth.currentUser?.updatePassword(password)
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

    fun deleteAccount() {
        val uid = fireBaseAuthClient.getFirebaseAuth.currentUser?.uid
        uid?.let { userId ->
            viewModelScope.launch {
                firebase
                    .collection("users")
                    .document(userId)
                    .delete()
                    .addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            fireBaseAuthClient.getFirebaseAuth.currentUser?.delete()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        updateCredentialChangeState(
                                            CredentialChangeResult.Success(R.string.account_deleted_message)
                                        )
                                    } else {
                                        task.exception?.let innerLet@{
                                            Log.d("idk", it.message.toString())
                                            if (it is FirebaseAuthRecentLoginRequiredException) {
                                                updateCredentialChangeState(
                                                    CredentialChangeResult.Failure.ReauthorizationRequired()
                                                )
                                                return@innerLet
                                            }

                                            updateCredentialChangeState(
                                                CredentialChangeResult.Failure.Error(it)
                                            )
                                        }
                                    }
                                }
                        } else {
                            Log.d("idk", deleteTask.exception?.message.toString())
                        }
                    }
            }
        }
    }
}

sealed interface Provider {
    data object Google : Provider
    data object Email : Provider
}

sealed class CredentialChangeResult {
    data class Success(@StringRes val message: Int) : CredentialChangeResult()
    sealed class Failure : CredentialChangeResult() {
        data class ReauthorizationRequired(
            val email: String? = null,
            val password: String? = null
        ) : Failure()

        data class Error(val exception: Exception) : Failure()
    }
}

@Stable
data class UserAccountDetails(
    val uid: String,
    val creationTimeStamp: Long?,
    val userName: String,
    val email: String,
    val isAnonymous: Boolean
)