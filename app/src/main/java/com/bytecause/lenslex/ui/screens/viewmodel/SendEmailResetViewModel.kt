package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.models.SimpleResult
import com.bytecause.lenslex.util.CredentialValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SendEmailResetViewModel(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _onResetRequestResult = MutableStateFlow<SimpleResult?>(null)
    val onResetRequestResult: StateFlow<SimpleResult?> = _onResetRequestResult.asStateFlow()

    private val _timer = MutableStateFlow(-1)
    val timer: StateFlow<Int> = _timer.asStateFlow()

    private val _credentialValidationResultState =
        MutableStateFlow<CredentialValidationResult?>(null)
    val credentialValidationResultState: StateFlow<CredentialValidationResult?> =
        _credentialValidationResultState.asStateFlow()

    private var sendPasswordResetEmailJob: Job? = null

    fun saveCredentialValidationResult(result: CredentialValidationResult) {
        _credentialValidationResultState.update {
            result
        }
    }

    fun updateResult(result: SimpleResult?) {
        _onResetRequestResult.value = result
    }

    private fun startTimer() {
        viewModelScope.launch(Dispatchers.IO) {
            val timer = 60 downTo 0
            for (x in timer) {
                _timer.update { x }
                delay(1_000)
                if (x == 0) _timer.update { -1 }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (sendPasswordResetEmailJob != null) return

        sendPasswordResetEmailJob = viewModelScope.launch {
            auth.getFirebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startTimer()
                        updateResult(SimpleResult.OnSuccess)
                    } else {
                        updateResult(SimpleResult.OnFailure(task.exception))
                    }
                }
        }.also { it.invokeOnCompletion { sendPasswordResetEmailJob = null } }
    }
}