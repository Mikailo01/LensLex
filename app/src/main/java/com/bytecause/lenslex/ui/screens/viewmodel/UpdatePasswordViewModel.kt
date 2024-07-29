package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.VerifyOobRepository
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEffect
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.screens.uistate.UpdatePasswordState
import com.bytecause.lenslex.util.ApiResult
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.NetworkUtil
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    private val auth: FirebaseAuthClient,
    private val verifyOobRepository: VerifyOobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatePasswordState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<UpdatePasswordUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    fun uiEventHandler(event: UpdatePasswordUiEvent) {
        when (event) {
            is UpdatePasswordUiEvent.OnPasswordVisibilityClick -> onPasswordVisibilityClick()
            is UpdatePasswordUiEvent.OnPasswordValueChange -> onPasswordValueChange(event.password)
            is UpdatePasswordUiEvent.OnConfirmPasswordValueChange -> onConfirmPasswordValueChange(
                event.confirmPassword
            )

            is UpdatePasswordUiEvent.OnVerifyOob -> onVerifyOob(event.oobCode)
            UpdatePasswordUiEvent.OnResetPasswordClick -> onResetPasswordClick()
            UpdatePasswordUiEvent.OnAnimationFinished -> onAnimationStarted()
            UpdatePasswordUiEvent.OnTryAgainClick -> onTryAgainClick()
            UpdatePasswordUiEvent.OnGetNewResetCodeClick -> sendEffect(UpdatePasswordUiEffect.GetNewResetCodeClick)
            UpdatePasswordUiEvent.OnDismiss -> onDismiss()
        }
    }

    private fun sendEffect(effect: UpdatePasswordUiEffect) {
        _effect.trySend(effect)
    }

    private fun onPasswordVisibilityClick() {
        _uiState.update {
            it.copy(
                passwordVisible = !it.passwordVisible
            )
        }
    }

    private fun onPasswordValueChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                credentialValidationResult = ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.PasswordCredential(
                        password = password,
                        confirmPassword = _uiState.value.confirmationPassword
                    )
                )
            )
        }
    }

    private fun onConfirmPasswordValueChange(password: String) {
        _uiState.update {
            it.copy(
                confirmationPassword = password,
                credentialValidationResult = ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.PasswordCredential(
                        password = _uiState.value.password,
                        confirmPassword = password
                    )
                )
            )
        }
    }

    private fun onVerifyOob(oobCode: String) {
        _uiState.update {
            it.copy(oobCode = oobCode)
        }
        verifyOob(oobCode)
    }

    private fun onResetPasswordClick() {
        ValidationUtil.areCredentialsValid(
            Credentials.Sensitive.PasswordCredential(
                password = _uiState.value.password,
                confirmPassword = _uiState.value.confirmationPassword
            )
        ).let { validationResult ->

            _uiState.update { it.copy(credentialValidationResult = validationResult) }

            if (validationResult is CredentialValidationResult.Valid) {
                _uiState.value.oobCode?.let { code ->
                    resetPassword(code, _uiState.value.password)
                }
            }
        }
    }

    private fun onAnimationStarted() {
        _uiState.update { it.copy(animationFinished = true) }
    }

    private fun onTryAgainClick() {
        _uiState.value.oobCode?.let { code ->
            verifyOob(code)
        }
    }

    private fun onDismiss() {
        _uiState.update { it.copy(dismissExpiredDialog = true) }
    }

    private fun resetPassword(oobCode: String, password: String) {
        auth.getAuth().confirmPasswordReset(oobCode, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEffect(UpdatePasswordUiEffect.ResetSuccessful)
                } else sendEffect(UpdatePasswordUiEffect.ResetFailure(task.exception))
            }
    }

    private fun verifyOob(oobCode: String) {
        viewModelScope.launch {
            when (val result = verifyOobRepository.verifyOob(oobCode)) {
                is ApiResult.Success -> {
                    when (result.data) {
                        true -> {
                            _uiState.update {
                                it.copy(
                                    codeValidationResult = CodeValidationResult(
                                        isLoading = false,
                                        result = CodeValidation.Valid
                                    )
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    showOobCodeExpiredDialog = true,
                                    codeValidationResult = CodeValidationResult(
                                        isLoading = false,
                                        result = CodeValidation.Invalid
                                    )
                                )
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    result.exception?.let {
                        _uiState.update {
                            it.copy(
                                codeValidationResult = CodeValidationResult(
                                    isLoading = false,
                                    error = if (!NetworkUtil.isOnline()) NetworkErrorType.NetworkUnavailable
                                    else NetworkErrorType.ServiceUnavailable
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    data class CodeValidationResult(
        val isLoading: Boolean = false,
        val result: CodeValidation? = null,
        val error: NetworkErrorType? = null
    )

    sealed interface CodeValidation {
        data object Valid : CodeValidation
        data object Invalid : CodeValidation
    }

    sealed interface NetworkErrorType {
        data object NetworkUnavailable : NetworkErrorType
        data object ServiceUnavailable : NetworkErrorType
    }
}