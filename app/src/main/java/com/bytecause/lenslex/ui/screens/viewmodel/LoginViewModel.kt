package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.ui.models.SignInResult
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

    fun uiEventHandler(event: LoginUiEvent.NonDirect) {
        when (event) {
            is LoginUiEvent.OnCredentialsEntered -> onCredentialsEnteredHandler()
            is LoginUiEvent.OnEmailValueChange -> onEmailValueChangeHandler(event.email)
            is LoginUiEvent.OnPasswordValueChange -> onPasswordValueChangeHandler(event.password)
            is LoginUiEvent.OnConfirmPasswordValueChange -> onConfirmPasswordValueChangeHandler(
                event.confirmationPassword
            )

            LoginUiEvent.OnPasswordsVisibilityChange -> onPasswordsVisibilityChangeHandler()
            LoginUiEvent.OnAnnotatedStringClick -> onAnnotatedStringClickHandler()
            LoginUiEvent.OnSignInAnonymously -> onSignInAnonymouslyHandler()
        }
    }

    private fun onCredentialsEnteredHandler() {
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

    private fun onEmailValueChangeHandler(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                credentialValidationResult = areCredentialsValid(
                    if (_uiState.value.signIn) {
                        Credentials.Sensitive.SignInCredentials(
                            email = email,
                            password = _uiState.value.password
                        )
                    } else {
                        Credentials.Sensitive.SignUpCredentials(
                            email = email,
                            password = _uiState.value.password,
                            confirmPassword = _uiState.value.confirmPassword
                        )
                    }
                )
            )
        }
    }

    private fun onPasswordValueChangeHandler(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                credentialValidationResult = areCredentialsValid(
                    if (_uiState.value.signIn) {
                        Credentials.Sensitive.SignInCredentials(
                            email = _uiState.value.email,
                            password = password
                        )
                    } else {
                        Credentials.Sensitive.SignUpCredentials(
                            email = _uiState.value.email,
                            password = password,
                            confirmPassword = _uiState.value.confirmPassword
                        )
                    }
                )
            )
        }
    }

    private fun onConfirmPasswordValueChangeHandler(password: String) {
        _uiState.update {
            it.copy(
                confirmPassword = password,
                credentialValidationResult = areCredentialsValid(
                    Credentials.Sensitive.SignUpCredentials(
                        email = _uiState.value.email,
                        password = _uiState.value.password,
                        confirmPassword = password
                    )
                )
            )
        }
    }

    private fun onPasswordsVisibilityChangeHandler() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    private fun onAnnotatedStringClickHandler() {
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

    private fun onSignInAnonymouslyHandler() {
        viewModelScope.launch {
            signInAnonymously().firstOrNull()?.let {
                onSignInResult(it)
            }
        }
    }

    fun onSignInResult(result: SignInResult) {
        _uiState.update {
            it.copy(
                signInState = it.signInState.copy(
                    isSignInSuccessful = (result.data != null),
                    signInError = result.errorMessage
                ),
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