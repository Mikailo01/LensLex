package com.bytecause.lenslex.ui.screens.viewmodel

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.SignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val fireBaseAuthClient: FireBaseAuthClient
) : ViewModel() {

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

    fun isUserSignedIn(): Boolean = fireBaseAuthClient.getSignedInUser() != null

    private fun onSignInResult(result: SignInResult) {
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

    suspend fun signInViaGoogle(): IntentSender? = fireBaseAuthClient.signInViaGoogle()

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

    suspend fun signOut() {
        fireBaseAuthClient.signOut()
    }

    fun areCredentialsValid(credentials: Credentials): CredentialValidationResult {
        return when (credentials) {
            is Credentials.SignInCredentials -> {

                val isEmailValid: Boolean = emailValidator(credentials.email)
                val isPasswordValid = passwordValidator(password = credentials.password)

                if (isEmailValid && isPasswordValid is PasswordValidationResult.Valid) CredentialValidationResult.Valid
                else CredentialValidationResult.Invalid(
                    isEmailValid = isEmailValid,
                    passwordError = isPasswordValid as? PasswordValidationResult.Invalid
                )
            }

            is Credentials.SignUpCredentials -> {

                val isEmailValid: Boolean = emailValidator(credentials.email)
                val passwordValidationResult =
                    passwordValidator(credentials.password, credentials.confirmPassword)


                return when {
                    isEmailValid && passwordValidationResult is PasswordValidationResult.Valid -> {
                        CredentialValidationResult.Valid
                    }

                    isEmailValid && passwordValidationResult is PasswordValidationResult.Invalid -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = true,
                            passwordError = passwordValidationResult.also {
                                Log.d(
                                    "idk2",
                                    it.cause.joinToString()
                                )
                            }
                        )
                    }

                    !isEmailValid && passwordValidationResult is PasswordValidationResult.Valid -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = isEmailValid,
                            passwordError = null
                        )
                    }

                    else -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = isEmailValid,
                            passwordError = passwordValidationResult
                        )
                    }

                }
            }
        }
    }

    private fun emailValidator(email: String): Boolean {
        val regex = Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")
        return regex.matches(email)
    }

    private fun passwordValidator(
        password: String,
        confirmPassword: String? = null
    ): PasswordValidationResult {
        val errors = mutableListOf<PasswordErrorType>()

        val isPasswordEmpty: Boolean = password.isBlank()

        val passwordMatch: Boolean? =
            if (confirmPassword != null) password == confirmPassword else null
        val passwordLengthInRange: Boolean = password.length in 8..24
        val upperCase: Boolean = password.any { it.isUpperCase() }
        val lowerCase: Boolean = password.any { it.isLowerCase() }
        val containsNumber: Boolean = password.any { it.isDigit() }

        if (isPasswordEmpty) errors.add(PasswordErrorType.PASSWORD_EMPTY)
        if (passwordMatch == false) errors.add(PasswordErrorType.PASSWORD_MISMATCH)
        if (!passwordLengthInRange) errors.add(PasswordErrorType.LENGTH_OUT_OF_BOUNDS)
        if (!upperCase) errors.add(PasswordErrorType.MISSING_UPPER_CASE)
        if (!lowerCase) errors.add(PasswordErrorType.MISSING_LOWER_CASE)
        if (!containsNumber) errors.add(PasswordErrorType.MISSING_DIGIT)

        return if (errors.isEmpty()) {
            PasswordValidationResult.Valid
        } else {
            errors.add(PasswordErrorType.PASSWORD_INCORRECT)
            PasswordValidationResult.Invalid(errors)
        }
    }
}

sealed interface CredentialValidationResult {
    data object Valid : CredentialValidationResult
    data class Invalid(val isEmailValid: Boolean?, val passwordError: PasswordValidationResult?) :
        CredentialValidationResult
}

sealed interface PasswordValidationResult {
    data object Valid : PasswordValidationResult
    data class Invalid(val cause: List<PasswordErrorType>) : PasswordValidationResult
}

enum class PasswordErrorType {
    PASSWORD_INCORRECT,
    PASSWORD_EMPTY,
    PASSWORD_MISMATCH,
    LENGTH_OUT_OF_BOUNDS,
    MISSING_LOWER_CASE,
    MISSING_UPPER_CASE,
    MISSING_DIGIT
}