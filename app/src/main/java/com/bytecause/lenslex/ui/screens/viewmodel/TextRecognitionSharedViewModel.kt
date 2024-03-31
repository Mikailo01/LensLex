package com.bytecause.lenslex.ui.screens.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TextRecognitionSharedViewModel : ViewModel() {

    private val _processedTextState = MutableStateFlow<List<String>>(emptyList())
    val processedTextState: StateFlow<List<String>> = _processedTextState.asStateFlow()

    fun updateProcessedTextState(text: List<String>) {
        _processedTextState.update {
            text
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("viewModel", "onCleared")
    }
}