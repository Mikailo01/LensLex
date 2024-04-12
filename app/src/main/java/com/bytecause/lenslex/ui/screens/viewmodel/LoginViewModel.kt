package com.bytecause.lenslex.ui.screens.viewmodel

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.Credentials
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

class LoginViewModel(
    private val fireBaseAuthClient: FireBaseAuthClient
) : ViewModel() {

    var signIn by mutableStateOf(true)
        private set

    fun signIn(boolean: Boolean) {
        signIn = boolean
    }

    var isLoading by mutableStateOf(false)
        private set

    fun isLoading(boolean: Boolean) {
        isLoading = boolean
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
                isSignInSuccessful = result.data != null,
                signInError = result.errorMessage
            )
        }
    }

    suspend fun signInWithGoogleIntent(intent: Intent) {
        val signInResult = fireBaseAuthClient.signInWithGoogleIntent(intent)
        onSignInResult(signInResult)
    }

    suspend fun signInViaGoogle(): IntentSender? = fireBaseAuthClient.signInViaGoogle().also { Log.d("idk", "fine" ) }

    suspend fun signInViaEmailAndPassword(credentials: Credentials.SignInCredentials) {
        if (!emailValidator(credentials.email) || credentials.password.isBlank()) return

        fireBaseAuthClient.signInViaEmailAndPassword(credentials.email, credentials.password)
            .firstOrNull()?.let {
                onSignInResult(it)
            }
    }

    suspend fun signUpViaEmailAndPassword(credentials: Credentials.SignUpCredentials): CredentialValidationResult {
        val validationResult = areCredentialsValid(
            Credentials.SignUpCredentials(
                email = credentials.email,
                password = credentials.password,
                confirmPassword = credentials.confirmPassword
            )
        )

        return if (validationResult is CredentialValidationResult.Valid
        ) {
            fireBaseAuthClient.signUpViaEmailAndPassword(credentials.email, credentials.password)
                .firstOrNull()?.let {
                    onSignInResult(it)
                }
            validationResult
        } else validationResult
    }

    suspend fun signInAnonymously() {
        fireBaseAuthClient.signInAnonymously().firstOrNull()?.let {
            onSignInResult(it)
        }
    }
}