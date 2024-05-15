package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.VerifyOobRepository
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.ui.screens.uistate.UpdatePasswordState
import com.bytecause.lenslex.util.ApiResult
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.NetworkUtil
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    private val auth: Authenticator,
    private val verifyOobRepository: VerifyOobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatePasswordState())
    val uiState = _uiState.asStateFlow()

    fun uiEventHandler(event: UpdatePasswordUiEvent) {
        when (event) {
            is UpdatePasswordUiEvent.OnPasswordVisibilityClick -> _uiState.update {
                it.copy(
                    passwordVisible = !it.passwordVisible
                )
            }

            is UpdatePasswordUiEvent.OnPasswordValueChange -> {
                _uiState.update {
                    it.copy(
                        password = event.password,
                        credentialValidationResult = ValidationUtil.areCredentialsValid(
                            Credentials.Sensitive.PasswordCredential(
                                password = event.password,
                                confirmPassword = _uiState.value.confirmationPassword
                            )
                        )
                    )
                }
            }

            is UpdatePasswordUiEvent.OnConfirmPasswordValueChange -> {
                _uiState.update {
                    it.copy(
                        confirmationPassword = event.confirmPassword,
                        credentialValidationResult = ValidationUtil.areCredentialsValid(
                            Credentials.Sensitive.PasswordCredential(
                                password = _uiState.value.password,
                                confirmPassword = event.confirmPassword
                            )
                        )
                    )
                }
            }

            is UpdatePasswordUiEvent.OnVerifyOob -> {
                _uiState.update {
                    it.copy(oobCode = event.oobCode)
                }
                verifyOob(event.oobCode)
            }

            UpdatePasswordUiEvent.OnResetPasswordClick -> {
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

            UpdatePasswordUiEvent.OnAnimationStarted -> _uiState.update { it.copy(animationStarted = true) }
            UpdatePasswordUiEvent.OnTryAgainClick -> {
                _uiState.value.oobCode?.let { code ->
                    verifyOob(code)
                }
            }

            UpdatePasswordUiEvent.OnGetNewResetCodeClick -> _uiState.update {
                it.copy(
                    showOobCodeExpiredDialog = false,
                    getNewCode = true
                )
            }

            UpdatePasswordUiEvent.OnDismiss -> _uiState.update { it.copy(dismissExpiredDialog = true) }
            UpdatePasswordUiEvent.OnResetPasswordResult -> updateState(null)
        }
    }

    fun resetCodeValidationState() {
        _uiState.update { it.copy(codeValidationResult = null) }
    }

    private fun updateState(state: SimpleResult?) {
        _uiState.update { it.copy(resetState = state) }
    }

    private fun resetPassword(oobCode: String, password: String) {
        auth.getAuth().confirmPasswordReset(oobCode, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateState(SimpleResult.OnSuccess)
                } else {
                    updateState(SimpleResult.OnFailure(task.exception))
                }
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