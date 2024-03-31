package com.bytecause.lenslex.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel

@Composable
fun AddScreen(
    viewModel: AddViewModel = hiltViewModel()
) {
    val languageOption by viewModel.languageOptionFlow.collectAsStateWithLifecycle(initialValue = "")

    var textFieldInput by rememberSaveable {
        mutableStateOf("")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            LanguagePreferences(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp),
                text = languageOption,
                onClick = {
                    viewModel.onSelectLanguageClick()
                }
            )
            TextField(
                value = textFieldInput,
                onValueChange = { textFieldInput = it },
                supportingText = {
                    Text(text = "Word")
                }
            )
            Button(onClick = { }) {
                Text(text = "Add")
            }
        }
    }
    if (viewModel.setShowLanguageDialog) {
        LanguageDialog(
            lazyListContent = viewModel.supportedLanguages,
            onDismiss = { viewModel.onDismissDialog() },
            onConfirm = {
                viewModel.setLangOption(it)
                viewModel.onDismissDialog()
            },
            onDownload = {
                // TODO()
            }
        )
    }
}