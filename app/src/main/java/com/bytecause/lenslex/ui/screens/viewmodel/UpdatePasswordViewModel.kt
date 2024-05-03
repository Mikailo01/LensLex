package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.data.repository.VerifyOobRepository
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.util.ApiResult
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.NetworkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    private val auth: AuthRepository,
    private val verifyOobRepository: VerifyOobRepository
) : ViewModel() {

    var resetState by mutableStateOf<SimpleResult?>(null)
        private set

    var codeValidationResultState by mutableStateOf<CodeValidationResult?>(CodeValidationResult(isLoading = true))
        private set

    private val _credentialValidationResultState =
        MutableStateFlow<CredentialValidationResult?>(null)
    val credentialValidationResultState: StateFlow<CredentialValidationResult?> =
        _credentialValidationResultState.asStateFlow()

    fun resetCodeValidationState() {
        codeValidationResultState = null
    }

    fun saveCredentialValidationResult(result: CredentialValidationResult) {
        _credentialValidationResultState.update {
            result
        }
    }

    fun updateState(state: SimpleResult?) {
        resetState = state
    }

    fun resetPassword(oobCode: String, password: String) {
        auth.getFirebaseAuth.confirmPasswordReset(oobCode, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateState(SimpleResult.OnSuccess)
                } else {
                    updateState(SimpleResult.OnFailure(task.exception))
                }
            }
    }

    fun verifyOob(oobCode: String) {
        viewModelScope.launch {
            codeValidationResultState = codeValidationResultState?.copy(isLoading = true)
            when (val result = verifyOobRepository.verifyOob(oobCode)) {
                is ApiResult.Success -> {
                    codeValidationResultState = when (result.data) {
                        true -> {
                            CodeValidationResult(isLoading = false, result = CodeValidation.Valid)
                        }

                        else -> {
                            CodeValidationResult(isLoading = false, result = CodeValidation.Invalid)
                        }
                    }
                }

                is ApiResult.Failure -> {
                    result.exception?.let {
                        codeValidationResultState = CodeValidationResult(
                            isLoading = false,
                            error = if (!NetworkUtil.isOnline()) NetworkErrorType.NetworkUnavailable
                            else NetworkErrorType.ServiceUnavailable
                        )
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