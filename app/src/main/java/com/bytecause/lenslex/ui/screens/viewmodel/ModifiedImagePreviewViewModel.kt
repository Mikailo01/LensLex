package com.bytecause.lenslex.ui.screens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.abstraction.TextRecognitionRepository
import com.bytecause.lenslex.ui.events.ModifiedImagePreviewUiEvent
import com.bytecause.lenslex.ui.screens.uistate.ModifiedImagePreviewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ModifiedImagePreviewViewModel(
    private val textRecognitionRepository: TextRecognitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModifiedImagePreviewState())
    val uiState = _uiState.asStateFlow()

    private val _textResultChannel = Channel<List<String>>()
    val textResultChannel = _textResultChannel.receiveAsFlow()

    fun uiEventHandler(event: ModifiedImagePreviewUiEvent.NonDirect) {
        when (event) {
            is ModifiedImagePreviewUiEvent.OnUpdateImage -> onUpdateImage(event.uri)
            is ModifiedImagePreviewUiEvent.OnProcessImageClick -> onProcessImageClick()
        }
    }

    private fun onUpdateImage(imageUri: Uri) {
        _uiState.update { it.copy(modifiedImageUri = imageUri, isButtonEnabled = true) }
    }

    private fun onProcessImageClick() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isProcessing = true)
            }

            runTextRecognition(listOf(_uiState.value.modifiedImageUri)).firstOrNull()
                ?.let { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isButtonEnabled = result.isNotEmpty(),
                            isImageTextless = result.isEmpty()
                        )
                    }
                    _textResultChannel.trySend(result)
                }
        }
    }

    private fun runTextRecognition(imagePaths: List<Uri>) =
        textRecognitionRepository.runTextRecognition(imagePaths)
}