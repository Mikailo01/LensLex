package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.abstraction.Authenticator
import com.bytecause.lenslex.ui.events.LoginUiEffect
import com.bytecause.lenslex.ui.models.SignInResult
import com.bytecause.lenslex.ui.screens.model.LoginState
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val auth: Authenticator
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<LoginUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    fun uiEventHandler(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnCredentialsEntered -> onCredentialsEntered()
            is LoginUiEvent.OnEmailValueChange -> onEmailValueChange(event.email)
            is LoginUiEvent.OnPasswordValueChange -> onPasswordValueChange(event.password)
            is LoginUiEvent.OnConfirmPasswordValueChange -> onConfirmPasswordValueChange(
                event.confirmationPassword
            )

            is LoginUiEvent.OnUpdateSignInResult -> onSignInResult(event.value)
            is LoginUiEvent.OnForgetPasswordClick -> sendEffect(LoginUiEffect.NavigateTo(event.screen))
            is LoginUiEvent.OnSignInUsingEmailAndPassword -> onSignInUsingEmailAndPassword(event.credentials)
            LoginUiEvent.OnPasswordsVisibilityChange -> onPasswordsVisibilityChange()
            LoginUiEvent.OnAnnotatedStringClick -> onAnnotatedStringClick()
            LoginUiEvent.OnSignInAnonymously -> onSignInAnonymously()
            LoginUiEvent.OnSignInUsingGoogle -> sendEffect(LoginUiEffect.SignInUsingGoogleIntent)
            LoginUiEvent.OnAnimationFinished -> onAnimationFinished()
            LoginUiEvent.OnCredentialManagerShown -> onCredentialManagerShown()
            LoginUiEvent.OnCredentialManagerDismiss -> onCredentialManagerDismiss()
        }
    }

    private fun sendEffect(effect: LoginUiEffect) {
        _effect.trySend(effect)
    }

    private fun onCredentialManagerDismiss() {
        _uiState.update {
            it.copy(shouldShowCredentialManager = false)
        }
    }

    private fun onAnimationFinished() {
        _uiState.update {
            it.copy(animationFinished = true)
        }
    }

    private fun onCredentialManagerShown() {
        _uiState.update {
            it.copy(credentialManagerShown = true)
        }
    }

    private fun onCredentialsEntered() {
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

    private fun onEmailValueChange(email: String) {
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

    private fun onPasswordValueChange(password: String) {
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

    private fun onConfirmPasswordValueChange(password: String) {
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

    private fun onPasswordsVisibilityChange() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    private fun onAnnotatedStringClick() {
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

    private fun onSignInUsingEmailAndPassword(credentials: Credentials.Sensitive.SignInCredentials) {
        viewModelScope.launch {
            signInUsingEmailAndPassword(credentials).firstOrNull()?.let {
                onSignInResult(it)
            }
        }
    }

    private fun onSignInAnonymously() {
        viewModelScope.launch {
            signInAnonymously().firstOrNull()?.let {
                onSignInResult(it)
            }
        }
    }

    private fun onSignInResult(result: SignInResult) {
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
    ): Flow<SignInResult> = auth.signInViaEmailAndPassword(credentials.email, credentials.password)

    private fun signUpViaEmailAndPassword(
        credentials: Credentials.Sensitive.SignUpCredentials
    ): Flow<SignInResult> =
        auth.signUpViaEmailAndPassword(credentials.email, credentials.password)

    private fun signInAnonymously(): Flow<SignInResult> = auth.signInAnonymously()
}