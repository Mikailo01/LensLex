package com.bytecause.lenslex.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.mlkit.Translator
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.util.Util.readJsonAsMapFromAssets
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreenContent(
    supportedLanguages: List<SupportedLanguage>,
    textFieldInput: String,
    selectedLanguage: SupportedLanguage,
    showLanguageDialog: Boolean,
    onTextFieldValueChange: (String) -> Unit,
    onSelectLanguageClick: () -> Unit,
    onInsertWord: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: (SupportedLanguage) -> Unit,
    onDownloadLanguage: (String) -> Unit,
    onRemoveLanguage: (String) -> Unit,
    onNavigateBack: () -> Unit
) {

    val context = LocalContext.current
    var isJobRunning by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.add_word,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) {
                onNavigateBack()
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                LanguagePreferences(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp),
                    text = selectedLanguage.langName,
                    onClick = {
                        onSelectLanguageClick()
                    }
                )
                TextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = textFieldInput,
                    onValueChange = { onTextFieldValueChange(it) },
                    supportingText = {
                        Text(text = "Word")
                    }
                )
                if (textFieldInput.isNotBlank()) {

                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            if (isJobRunning) return@Button
                            isJobRunning = true

                            // Returns Map with abbreviation words.
                            val jsonContent =
                                readJsonAsMapFromAssets(context, "abbreviations_wordlist.json")

                            Translator.translate(
                                text = jsonContent?.get(textFieldInput.lowercase())
                                    ?: textFieldInput,
                                sourceLang = "en",
                                targetLang = selectedLanguage.langCode
                            ) { translationResult ->

                                when (translationResult) {
                                    Translator.TranslationResult.ModelDownloadFailure -> {
                                        Toast.makeText(
                                            context,
                                            "Model download failed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isJobRunning = false
                                    }

                                    is Translator.TranslationResult.TranslationSuccess -> {
                                        onInsertWord(translationResult.translatedText)
                                    }

                                    Translator.TranslationResult.TranslationFailure -> {
                                        Toast.makeText(
                                            context,
                                            "Translation failed.",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        isJobRunning = false
                                    }
                                }
                            }
                        }) {
                        Text(text = stringResource(id = R.string.add))
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageDialog(
            lazyListContent = supportedLanguages,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            onDismiss = { onDismissDialog() },
            onConfirm = {
                onConfirmDialog(it)
            },
            onDownload = { langCode ->
                onDownloadLanguage(langCode)
            },
            onRemove = { langCode ->
                onRemoveLanguage(langCode)
            }
        )
    }
}

@Composable
fun AddScreen(
    viewModel: AddViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val selectedLanguage by viewModel.languageOptionFlow.collectAsStateWithLifecycle(
        initialValue = SupportedLanguage()
    )
    val supportedLanguages by viewModel.supportedLanguages.collectAsStateWithLifecycle()

    var textFieldInput by rememberSaveable {
        mutableStateOf("")
    }

    var showLanguageDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddScreenContent(
        supportedLanguages = supportedLanguages,
        textFieldInput = textFieldInput,
        selectedLanguage = selectedLanguage,
        showLanguageDialog = showLanguageDialog,
        onTextFieldValueChange = {
            textFieldInput = it
        },
        onSelectLanguageClick = {
            showLanguageDialog = true
        },
        onInsertWord = { translatedText ->
            viewModel.insertWord(
                WordsAndSentences(
                    id = "${textFieldInput}_en".lowercase()
                        .replace(" ", "_"),
                    word = textFieldInput,
                    languageCode = "en",
                    translations = mapOf(selectedLanguage.langCode to translatedText),
                    timeStamp = System.currentTimeMillis()
                )
            ) {
                onNavigateBack()
            }
        },
        onDismissDialog = {
            showLanguageDialog = false
        },
        onConfirmDialog = {
            viewModel.setLangOption(it)
            showLanguageDialog = false
        },
        onDownloadLanguage = { langCode ->
            viewModel.downloadModel(langCode)
        },
        onRemoveLanguage = { langCode ->
            viewModel.removeModel(langCode)
        },
        onNavigateBack = {
            onNavigateBack()
        }
    )

}

@Preview
@Composable
fun AddScreenPreview() {
    AddScreenContent(
        supportedLanguages = emptyList(),
        textFieldInput = "",
        selectedLanguage = SupportedLanguage("cs", "Czech"),
        showLanguageDialog = false,
        onTextFieldValueChange = {},
        onSelectLanguageClick = {},
        onInsertWord = {},
        onDismissDialog = {},
        onConfirmDialog = {},
        onDownloadLanguage = {},
        onRemoveLanguage = {},
        onNavigateBack = {}
    )
}