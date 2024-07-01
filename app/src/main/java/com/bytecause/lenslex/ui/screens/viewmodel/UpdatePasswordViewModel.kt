package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.abstraction.VerifyOobRepository
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.SimpleResult
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
    private val auth: Authenticator,
    private val verifyOobRepository: VerifyOobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatePasswordState())
    val uiState = _uiState.asStateFlow()

    private val _getNewCodeChannel = Channel<Boolean>()
    val getNewCodeChannel = _getNewCodeChannel.receiveAsFlow()

    fun uiEventHandler(event: UpdatePasswordUiEvent) {
        when (event) {
            is UpdatePasswordUiEvent.OnPasswordVisibilityClick -> onPasswordVisibilityClickHandler()
            is UpdatePasswordUiEvent.OnPasswordValueChange -> onPasswordValueChangeHandler(event.password)
            is UpdatePasswordUiEvent.OnConfirmPasswordValueChange -> onConfirmPasswordValueChangeHandler(
                event.confirmPassword
            )

            is UpdatePasswordUiEvent.OnVerifyOob -> onVerifyOobHandler(event.oobCode)
            UpdatePasswordUiEvent.OnResetPasswordClick -> onResetPasswordClickHandler()
            UpdatePasswordUiEvent.OnAnimationStarted -> onAnimationStartedHandler()
            UpdatePasswordUiEvent.OnTryAgainClick -> onTryAgainClickHandler()
            UpdatePasswordUiEvent.OnGetNewResetCodeClick -> onGetNewResetCodeClickHandler()
            UpdatePasswordUiEvent.OnDismiss -> onDismissHandler()
            UpdatePasswordUiEvent.OnResetPasswordResult -> updateState(null)
        }
    }

    private fun onPasswordVisibilityClickHandler() {
        _uiState.update {
            it.copy(
                passwordVisible = !it.passwordVisible
            )
        }
    }

    private fun onPasswordValueChangeHandler(password: String) {
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

    private fun onConfirmPasswordValueChangeHandler(password: String) {
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

    private fun onVerifyOobHandler(oobCode: String) {
        _uiState.update {
            it.copy(oobCode = oobCode)
        }
        verifyOob(oobCode)
    }

    private fun onGetNewResetCodeClickHandler() {
        _getNewCodeChannel.trySend(true)
    }

    private fun onResetPasswordClickHandler() {
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

    private fun onAnimationStartedHandler() {
        _uiState.update { it.copy(animationStarted = true) }
    }

    private fun onTryAgainClickHandler() {
        _uiState.value.oobCode?.let { code ->
            verifyOob(code)
        }
    }

    private fun onDismissHandler() {
        _uiState.update { it.copy(dismissExpiredDialog = true) }
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