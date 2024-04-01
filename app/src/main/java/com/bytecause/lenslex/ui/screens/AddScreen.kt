package com.bytecause.lenslex.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
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
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.mlkit.Translator
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: AddViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val selectedLanguage by viewModel.languageOptionFlow.collectAsStateWithLifecycle(
        initialValue = SupportedLanguage(
            "",
            ""
        )
    )

    var textFieldInput by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.add_word,
                navigationIcon = Icons.Filled.ArrowBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                onNavigateBack()
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                LanguagePreferences(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp),
                    text = selectedLanguage.langName,
                    onClick = {
                        viewModel.onSelectLanguageClick()
                    }
                )
                TextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = textFieldInput,
                    onValueChange = { textFieldInput = it },
                    supportingText = {
                        Text(text = "Word")
                    }
                )
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        Translator.translate(
                            text = textFieldInput,
                            sourceLang = "en",
                            targetLang = selectedLanguage.langCode
                        ) {
                            viewModel.insertOrUpdateWordAndSentenceEntity(
                                WordAndSentenceEntity(
                                    words = textFieldInput,
                                    languageCode = "en",
                                    translations = mapOf(selectedLanguage.langCode to it)
                                )
                            )
                            onNavigateBack()
                        }
                    }) {
                    Text(text = "Add")
                }
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