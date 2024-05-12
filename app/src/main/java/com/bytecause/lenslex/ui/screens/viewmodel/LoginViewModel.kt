package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.domain.models.SignInResult
import com.bytecause.lenslex.domain.models.SignInState
import com.bytecause.lenslex.ui.screens.uistate.LoginState
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
    private val auth: Authenticator
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private val _signUiState = MutableStateFlow(SignInState())
    val signUiState = _signUiState.asStateFlow()

    fun uiEventHandler(event: LoginUiEvent.NonDirect) {
        when (event) {
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
                                        if (it.errorMessage != null) {
                                            _uiState.update { state ->
                                                state.copy(isLoading = false)
                                            }
                                        }
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
                                        if (it.errorMessage != null) {
                                            _uiState.update { state ->
                                                state.copy(isLoading = false)
                                            }
                                        }
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
                        email = event.email,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = event.email,
                                    password = _uiState.value.password
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = event.email,
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
                        password = event.password,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = _uiState.value.email,
                                    password = event.password
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = _uiState.value.email,
                                    password = event.password,
                                    confirmPassword = _uiState.value.confirmPassword
                                )
                            }
                        )
                    )
                }
            }

            is LoginUiEvent.OnConfirmPasswordValueChange -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.confirmationPassword,
                        credentialValidationResult = areCredentialsValid(
                            Credentials.Sensitive.SignUpCredentials(
                                email = _uiState.value.email,
                                password = _uiState.value.password,
                                confirmPassword = event.confirmationPassword
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
                    signInAnonymously().firstOrNull()?.let {
                        onSignInResult(it)
                    }
                }
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

    private fun signInAnonymously(): Flow<SignInResult> = auth.signInAnonymously()
}