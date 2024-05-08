package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.models.uistate.SendEmailResetState
import com.bytecause.lenslex.ui.events.SendEmailResetUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SendEmailResetViewModel(
    private val auth: Authenticator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendEmailResetState())
    val uiState = _uiState.asStateFlow()

    private var sendPasswordResetEmailJob: Job? = null

    fun uiEventHandler(event: SendEmailResetUiEvent) {
        when (event) {
            is SendEmailResetUiEvent.OnEmailValueChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        isEmailError = ValidationUtil.areCredentialsValid(
                            Credentials.Sensitive.EmailCredential(event.value)
                        ) is CredentialValidationResult.Invalid
                    )
                }
            }

            SendEmailResetUiEvent.OnSendEmailClick -> {
                val result = ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.EmailCredential(_uiState.value.email)
                )

                if (result is CredentialValidationResult.Valid) {
                    sendPasswordResetEmail(_uiState.value.email)
                } else {
                    _uiState.update { it.copy(isEmailError = true) }
                }
            }

            SendEmailResetUiEvent.OnAnimationStarted -> {
                _uiState.update { it.copy(animationStarted = true) }
            }
        }
    }

    fun animationLaunched() {
        _uiState.update { it.copy(animationStarted = true) }
    }

    fun updateRequestResult(result: SimpleResult?) {
        _uiState.update { it.copy(requestResult = result) }
    }

    private fun startTimer() {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = 60 downTo 0
            for (x in timer) {
                _uiState.update { it.copy(timer = x) }
                delay(1_000)
                if (x == 0) _uiState.update { it.copy(timer = -1) }
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        if (sendPasswordResetEmailJob != null) return

        sendPasswordResetEmailJob = viewModelScope.launch {
            auth.getAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startTimer()
                        updateRequestResult(SimpleResult.OnSuccess)
                    } else {
                        updateRequestResult(SimpleResult.OnFailure(task.exception))
                    }
                }
        }.also { it.invokeOnCompletion { sendPasswordResetEmailJob = null } }
    }
}