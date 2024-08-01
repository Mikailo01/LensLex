package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.ui.events.SendEmailResetUiEffect
import com.bytecause.lenslex.ui.events.SendEmailResetUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.screens.uistate.SendEmailResetState
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SendEmailResetViewModel(
    private val auth: FirebaseAuthClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendEmailResetState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<SendEmailResetUiEffect>(capacity = Channel.CONFLATED)
    val effect = _effect.receiveAsFlow()

    private var shouldSendResetPasswordEmail: Boolean = true

    fun uiEventHandler(event: SendEmailResetUiEvent) {
        when (event) {
            is SendEmailResetUiEvent.OnEmailValueChanged -> onEmailValueChanged(event.value)
            SendEmailResetUiEvent.OnSendEmailClick -> onSendEmailClick()
            SendEmailResetUiEvent.OnAnimationFinished -> onAnimationFinished()
            SendEmailResetUiEvent.OnNavigateBack -> sendEffect(SendEmailResetUiEffect.NavigateBack)
        }
    }

    private fun sendEffect(effect: SendEmailResetUiEffect) {
        _effect.trySend(effect)
    }

    private fun onEmailValueChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                isEmailError = ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.EmailCredential(email)
                ) is CredentialValidationResult.Invalid
            )
        }
    }

    private fun onSendEmailClick() {
        val result = ValidationUtil.areCredentialsValid(
            Credentials.Sensitive.EmailCredential(_uiState.value.email)
        )

        if (result is CredentialValidationResult.Valid) {
            sendPasswordResetEmail(_uiState.value.email)
        } else {
            _uiState.update { it.copy(isEmailError = true) }
        }
    }

    private fun onAnimationFinished() {
        _uiState.update { it.copy(animationFinished = true) }
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
        if (!shouldSendResetPasswordEmail) return
        shouldSendResetPasswordEmail = false

        auth.getAuth().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startTimer()

                    sendEffect(SendEmailResetUiEffect.SuccessfulRequest)
                    shouldSendResetPasswordEmail = true
                } else {
                    sendEffect(SendEmailResetUiEffect.FailureRequest(task.exception))
                    shouldSendResetPasswordEmail = true
                }
            }
    }
}