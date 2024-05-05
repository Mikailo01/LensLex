package com.bytecause.lenslex.ui.screens.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.SignInState
import com.bytecause.lenslex.models.uistate.LoginState
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val auth: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    /* private val credentialManager by lazy {
         CredentialManager.create(application)
     }*/

    private val _signUiState = MutableStateFlow(SignInState())
    val signUiState = _signUiState.asStateFlow()

    fun uiEventHandler(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnCredentialChanged -> {
                _uiState.update {
                    it.copy(
                        credentialValidationResult = areCredentialsValid(event.value)
                    )
                }
            }

            is LoginUiEvent.OnCredentialsEntered -> {
                areCredentialsValid(
                    if (_uiState.value.signIn) Credentials.Sensitive.SignInCredentials(
                        _uiState.value.email,
                        _uiState.value.password
                    ) else Credentials.Sensitive.SignUpCredentials(
                        email = _uiState.value.email,
                        password = _uiState.value.password,
                        confirmPassword = _uiState.value.confirmPassword
                    )
                ).let { validationResult ->

                    _uiState.update {
                        it.copy(credentialValidationResult = validationResult)
                    }

                    if (validationResult is CredentialValidationResult.Valid) {
                        viewModelScope.launch {
                            when (_uiState.value.signIn) {
                                true -> {
                                    _uiState.update {
                                        it.copy(isLoading = true)
                                    }

                                    signInUsingEmailAndPassword(
                                        Credentials.Sensitive.SignInCredentials(
                                            email = _uiState.value.email,
                                            password = _uiState.value.password
                                        )
                                    ).firstOrNull()?.let {
                                        onSignInResult(it)
                                    }
                                }

                                false -> {
                                    _uiState.update {
                                        it.copy(isLoading = true)
                                    }

                                    signUpViaEmailAndPassword(
                                        Credentials.Sensitive.SignUpCredentials(
                                            email = _uiState.value.email,
                                            password = _uiState.value.password,
                                            confirmPassword = _uiState.value.confirmPassword
                                        )
                                    ).firstOrNull()?.let {
                                        onSignInResult(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is LoginUiEvent.OnEmailValueChange -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = event.value,
                                    password = _uiState.value.password
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = event.value,
                                    password = _uiState.value.password,
                                    confirmPassword = _uiState.value.confirmPassword
                                )
                            }
                        )
                    )
                }
            }

            is LoginUiEvent.OnPasswordValueChange -> {
                _uiState.update {
                    it.copy(
                        password = event.value,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = _uiState.value.email,
                                    password = event.value
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = _uiState.value.email,
                                    password = event.value,
                                    confirmPassword = _uiState.value.confirmPassword
                                )
                            }
                        )
                    )
                }
            }

            is LoginUiEvent.OnConfirmPasswordChange -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.value,
                        credentialValidationResult = areCredentialsValid(
                            Credentials.Sensitive.SignUpCredentials(
                                email = _uiState.value.email,
                                password = _uiState.value.password,
                                confirmPassword = event.value
                            )
                        )
                    )
                }
            }

            LoginUiEvent.OnPasswordsVisibilityChange -> {
                _uiState.update {
                    it.copy(passwordVisible = !it.passwordVisible)
                }
            }

            LoginUiEvent.OnAnnotatedStringClick -> {
                _uiState.update {
                    it.copy(
                        email = "",
                        password = "",
                        confirmPassword = "",
                        credentialValidationResult = null,
                        signIn = !it.signIn
                    )
                }
            }

            LoginUiEvent.OnSignInAnonymously -> {
                viewModelScope.launch {
                    signInAnonymously()
                }
            }

            else -> {
                // do nothing
            }
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

    private fun signInUsingEmailAndPassword(
        credentials: Credentials.Sensitive.SignInCredentials
    ): Flow<SignInResult> =
        auth.signInViaEmailAndPassword(credentials.email, credentials.password)

    private fun signUpViaEmailAndPassword(
        credentials: Credentials.Sensitive.SignUpCredentials
    ): Flow<SignInResult> =
        auth.signUpViaEmailAndPassword(credentials.email, credentials.password)

    private suspend fun signInAnonymously() {
        auth.signInAnonymously().firstOrNull()?.let {
            onSignInResult(it)
        }
    }
}