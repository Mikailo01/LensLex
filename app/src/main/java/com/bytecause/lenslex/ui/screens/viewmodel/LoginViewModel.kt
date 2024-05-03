package com.bytecause.lenslex.ui.screens.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.SignInState
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import com.bytecause.lenslex.util.ValidationUtil.emailValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val auth: AuthRepository
) : AndroidViewModel(application) {

    private val credentialManager by lazy {
        CredentialManager.create(application)
    }

    private val _signUiState = MutableStateFlow(SignInState())
    val signUiState = _signUiState.asStateFlow()

    private val _credentialValidationResultState =
        MutableStateFlow<CredentialValidationResult?>(null)
    val credentialValidationResultState: StateFlow<CredentialValidationResult?> =
        _credentialValidationResultState.asStateFlow()

    fun saveCredentialValidationResult(result: CredentialValidationResult) {
        _credentialValidationResultState.update {
            result
        }
    }

    fun onSignInResult(result: SignInResult) {
        _signUiState.update {
            _signUiState.value.copy(
                isSignInSuccessful = (result.data != null),
                signInError = result.errorMessage
            )
        }
    }

    fun signInUsingGoogleCredential(context: Context) {
        viewModelScope.launch {
            onSignInResult(auth.signInUsingGoogleCredential(context))
        }
    }

    private suspend fun saveCredential(context: Context, username: String, password: String) {
        try {
            // Ask the user for permission to add the credentials to their store
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(username, password)
            )
            Log.v("CredentialTest", "Credentials successfully added")
        } catch (e: CreateCredentialCancellationException) {
            // do nothing, the user chose not to save the credential
            Log.v("CredentialTest", "User cancelled the save")
        } catch (e: CreateCredentialException) {
            Log.v("CredentialTest", "Credential save error", e)
        }
    }

    private suspend fun getCredential(context: Context): PasswordCredential? {
        try {
            // GetPasswordOption() tell the credential library that we're only interested in password credentials
            // Show the user a dialog allowing them to pick a saved credential
            val credentialResponse = credentialManager.getCredential(
                request = GetCredentialRequest(
                    listOf(GetPasswordOption())
                ),
                context = context
            )

            // Return the selected credential (as long as it's a username/password)
            return credentialResponse.credential as? PasswordCredential
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the request. Return nothing
            return null
        } catch (e: NoCredentialException) {
            // We don't have a matching credential
            return null
        } catch (e: GetCredentialException) {
            Log.e("CredentialTest", "Error getting credential", e)
            throw e
        }
    }

    fun signInWithSavedCredential(context: Context) {
        viewModelScope.launch {
            try {
                val passwordCredential = getCredential(context) ?: return@launch

                signInUsingEmailAndPassword(
                    Credentials.Sensitive.SignInCredentials(
                        email = passwordCredential.id,
                        password = passwordCredential.password
                    )
                )?.let {
                    onSignInResult(it)
                }
            } catch (e: Exception) {
                Log.e("CredentialTest", "Error getting credential", e)
            }
        }
    }

    private suspend fun signInUsingEmailAndPassword(
        credentials: Credentials.Sensitive.SignInCredentials
    ): SignInResult? {
        return auth.signInViaEmailAndPassword(credentials.email, credentials.password)
            .firstOrNull()
    }

    suspend fun signInViaEmailAndPasswordIfValid(
        context: Context,
        credentials: Credentials.Sensitive.SignInCredentials
    ) {
        if (!emailValidator(credentials.email) || credentials.password.isBlank()) return

        signInUsingEmailAndPassword(credentials)?.let {
            if (it.data != null) saveCredential(
                context = context,
                credentials.email,
                credentials.password
            )
            onSignInResult(it)
        }
    }

    suspend fun signUpViaEmailAndPassword(
        context: Context,
        credentials: Credentials.Sensitive.SignUpCredentials
    ): CredentialValidationResult {
        val validationResult = areCredentialsValid(
            Credentials.Sensitive.SignUpCredentials(
                email = credentials.email,
                password = credentials.password,
                confirmPassword = credentials.confirmPassword
            )
        )

        return if (validationResult is CredentialValidationResult.Valid
        ) {
            auth.signUpViaEmailAndPassword(credentials.email, credentials.password)
                .firstOrNull()?.let {
                    onSignInResult(it)
                    if (it.data != null) saveCredential(
                        context = context,
                        credentials.email,
                        credentials.password
                    )
                }
            validationResult
        } else validationResult
    }

    suspend fun signInAnonymously() {
        auth.signInAnonymously().firstOrNull()?.let {
            onSignInResult(it)
        }
    }
}