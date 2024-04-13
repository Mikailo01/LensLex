package com.bytecause.lenslex.ui.screens.viewmodel

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.ui.components.AccountInfoType
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.util.CredentialValidationResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.lang.Exception

class AccountSettingsViewModel(
    private val fireBaseAuthClient: FireBaseAuthClient
) : ViewModel() {

    private val _credentialChangeState = MutableStateFlow<CredentialChangeResult?>(null)
    val credentialChangeState: StateFlow<CredentialChangeResult?> =
        _credentialChangeState.asStateFlow()

    fun resetCredentialChangeState() {
        _credentialChangeState.value = null
    }

    private val _showCredentialUpdateDialog = MutableStateFlow<CredentialType?>(null)
    val showCredentialUpdateDialog = _showCredentialUpdateDialog.asStateFlow()

    private val _credentialValidationResultState =
        MutableStateFlow<CredentialValidationResult?>(null)
    val credentialValidationResultState: StateFlow<CredentialValidationResult?> =
        _credentialValidationResultState.asStateFlow()

    fun saveCredentialValidationResult(result: CredentialValidationResult) {
        _credentialValidationResultState.update {
            result
        }
    }

    suspend fun signInWithGoogleIntent(intent: Intent) {
        val signInResult = fireBaseAuthClient.signInWithGoogleIntent(intent)
        if (signInResult.data != null) {
            _getProviders.value = _getProviders.value + Provider.Google
            reload()
        }
    }

    suspend fun signInViaGoogle(): IntentSender? = fireBaseAuthClient.signInViaGoogle()

    suspend fun reauthorizeWithGoogle(intent: Intent) {
        val reauthenticateResult = fireBaseAuthClient.reauthenticateWithGoogleIntent(intent)
        if (reauthenticateResult.data != null) {
            resetCredentialChangeState()
            reload()
        }
    }

    private val _getProviders = MutableStateFlow(
        fireBaseAuthClient.getSignedInUser()?.let { user ->
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

    fun unlinkProvider(provider: Provider) {
        when (provider) {
            Provider.Google -> {
                fireBaseAuthClient.getSignedInUser()
                    ?.unlink(GoogleAuthProvider.PROVIDER_ID)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            _getProviders.value =
                                _getProviders.value.filter { it != Provider.Google }

                            /*_getUserAccountDetails.value = _getUserAccountDetails.value?.copy(
                                email = fireBaseAuthClient.getSignedInUser()?.providerData?.find { it.email?.isNotBlank() == true }?.email
                                    ?: "Not set"
                            )*/

                            reload()
                        }
                    }
            }

            Provider.Email -> {
                fireBaseAuthClient.getSignedInUser()
                    ?.unlink(EmailAuthProvider.PROVIDER_ID)?.addOnCompleteListener {
                        Log.d("idk", it.exception.toString())
                        if (it.isSuccessful) {
                            _getProviders.value =
                                _getProviders.value.filter { it != Provider.Email }
                            /* _getUserAccountDetails.value = _getUserAccountDetails.value?.copy(
                                 email = fireBaseAuthClient.getSignedInUser()?.providerData?.find { it.email?.isNotBlank() == true }?.email
                                     ?: "Not set"*/
                            reload()
                        }
                    }
            }
        }
    }

    fun linkProvider(credentials: Credentials?, provider: Provider) {
        when (provider) {
            Provider.Google -> {
                // fireBaseAuthClient.getSignedInUser()?.linkWithCredential()
            }

            Provider.Email -> {
                val credential = (credentials as Credentials.SignInCredentials)
                val emailCredentials =
                    EmailAuthProvider.getCredential(credential.email, credential.password)
                fireBaseAuthClient.getSignedInUser()?.linkWithCredential(emailCredentials)
                    ?.addOnCompleteListener {
                        Log.d("idk", it.exception.toString())
                        if (it.isSuccessful) {
                            _getProviders.value = _getProviders.value + Provider.Email

                            /* _getUserAccountDetails.value =
                                 _getUserAccountDetails.value?.copy(email = fireBaseAuthClient.getSignedInUser()?.email.takeIf { it?.isNotBlank() == true }
                                     ?: "Not set")*/

                            reload()

                            Log.d("idk", _getProviders.value.joinToString())
                        } else {

                        }
                    }
            }
        }
    }

    private val _getUserAccountDetails =
        MutableStateFlow(fireBaseAuthClient.getSignedInUser()?.run {
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

    fun showCredentialUpdateDialog(credentialType: CredentialType?) {
        _showCredentialUpdateDialog.value = credentialType
    }

    private fun reload() {
        fireBaseAuthClient.getSignedInUser()?.reload()

        Log.d("idk22", fireBaseAuthClient.getSignedInUser()?.email.toString())

        _getUserAccountDetails.value = fireBaseAuthClient.getSignedInUser()?.run {
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

    fun handleRequest(request: AccountInfoType) {
        when (request) {

            is AccountInfoType.UserName -> {
                // updateUserName()
            }

            is AccountInfoType.Email -> {

            }

            is AccountInfoType.Password -> {

            }

            else -> return
        }
    }

    fun updateUserName(userName: String) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()

        fireBaseAuthClient.getSignedInUser()?.updateProfile(changeRequest)
        _getUserAccountDetails.value =
            _getUserAccountDetails.value?.copy(userName = userName)
    }

    fun reauthenticateUsingEmailAndPassword(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        fireBaseAuthClient.getSignedInUser()?.reauthenticate(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (credentialChangeState.value is CredentialChangeResult.Failure.ReauthorizationRequired) {
                        val newEmail =
                            (credentialChangeState.value as CredentialChangeResult.Failure.ReauthorizationRequired).email
                        val newPassword =
                            (credentialChangeState.value as CredentialChangeResult.Failure.ReauthorizationRequired).password

                        when {
                            newEmail != null -> {
                                updateEmail(newEmail)
                            }

                            newPassword != null -> {
                                updatePassword(newPassword)
                            }
                        }
                    }
                    resetCredentialChangeState()
                    reload()
                } else {

                }
            }
    }

    fun updateEmail(email: String) {
        fireBaseAuthClient.getSignedInUser()?.verifyBeforeUpdateEmail(email)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    _credentialChangeState.value =
                        CredentialChangeResult.Success("Email updated successfully.")
                    _getUserAccountDetails.value =
                        _getUserAccountDetails.value?.copy(email = email)
                } else {
                    when (it.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            _credentialChangeState.value =
                                CredentialChangeResult.Failure.Error(it.exception as FirebaseAuthInvalidUserException)
                        }

                        is FirebaseAuthRecentLoginRequiredException -> {
                            _credentialChangeState.value =
                                CredentialChangeResult.Failure.ReauthorizationRequired(email = email)
                        }
                    }
                }
            }
    }

    fun updatePassword(password: String) {
        fireBaseAuthClient.getSignedInUser()?.updatePassword(password)?.addOnCompleteListener {
            if (it.isSuccessful) {
                _credentialChangeState.value =
                    CredentialChangeResult.Success("Password updated successfully.")
            } else {
                when (it.exception) {
                    is FirebaseAuthInvalidUserException -> {
                        _credentialChangeState.value =
                            CredentialChangeResult.Failure.Error(it.exception as FirebaseAuthInvalidUserException)
                    }

                    is FirebaseAuthRecentLoginRequiredException -> {
                        _credentialChangeState.value =
                            CredentialChangeResult.Failure.ReauthorizationRequired(password = password)
                    }
                }
            }

        }
    }

    fun deleteAccount() {
        fireBaseAuthClient.getSignedInUser()?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                _credentialChangeState.value = CredentialChangeResult.Success("")
            } else {
                when (it.exception) {
                    is FirebaseAuthInvalidUserException -> {
                        _credentialChangeState.value =
                            CredentialChangeResult.Failure.Error(it.exception as FirebaseAuthInvalidUserException)
                    }

                    is FirebaseAuthRecentLoginRequiredException -> {
                        _credentialChangeState.value =
                            CredentialChangeResult.Failure.ReauthorizationRequired()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("idk", "cleared")
    }
}

sealed interface Provider {
    data object Google : Provider
    data object Email : Provider
}

sealed class CredentialChangeResult {
    data class Success(val message: String) : CredentialChangeResult()
    sealed class Failure : CredentialChangeResult() {
        data class ReauthorizationRequired(
            val email: String? = null,
            val password: String? = null
        ) : Failure()

        data class Error(val exception: Exception) : Failure()
    }
}

/*data class CredentialChangeResult(
    val success: Boolean? = null,
    val failedTask: FailedTask? = null
)

data class FailedTask(
    val email: String?,
    val password: String?,
    val exception: Exception
)*/

@Stable
data class UserAccountDetails(
    val uid: String,
    val creationTimeStamp: Long?,
    val userName: String,
    val email: String,
    val isAnonymous: Boolean
)