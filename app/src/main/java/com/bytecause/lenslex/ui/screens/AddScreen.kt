package com.bytecause.lenslex.ui.screens

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NetworkUnavailableDialog
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.AddUiEffect
import com.bytecause.lenslex.ui.events.AddUiEvent
import com.bytecause.lenslex.ui.screens.model.AddState
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.util.Util.readJsonAsMapFromAssets
import com.ehsanmsz.mszprogressindicator.progressindicator.BallGridPulseProgressIndicator
import org.koin.androidx.compose.koinViewModel

private const val AbbreviationsJsonName = "abbreviations_wordlist.json"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreenContent(
    state: AddState,
    onEvent: (AddUiEvent) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.add_word,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) {
                onEvent(AddUiEvent.OnNavigateBack)
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
                    originLangName = state.selectedLanguageOptions.first.lang.langName,
                    targetLangName = state.selectedLanguageOptions.second.lang.langName,
                    onClick = {
                        onEvent(AddUiEvent.OnShowLanguageDialog(it))
                    },
                    onSwitchLanguages = {
                        onEvent(AddUiEvent.OnSwitchLanguages)
                    }
                )
                TextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = state.textValue,
                    label = {
                        Text(text = stringResource(id = R.string.type_word_or_sentence))
                    },
                    onValueChange = { onEvent(AddUiEvent.OnTextValueChange(it)) },
                    supportingText = {
                        Text(text = stringResource(id = R.string.word))
                    }
                )

                if (state.textValue.isNotBlank()) {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            // Returns Map with abbreviation words.
                            val jsonContent =
                                readJsonAsMapFromAssets(context, AbbreviationsJsonName)

                            onEvent(
                                AddUiEvent.OnTranslate(
                                    (jsonContent?.get(state.textValue.lowercase())
                                        ?: state.textValue).trimIndent()
                                )
                            )
                        }) {
                        Text(text = stringResource(id = R.string.add))
                    }
                }
            }
            if (state.isLoading) {
                BallGridPulseProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            SnackbarHost(
                hostState = state.snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }

    if (state.showNetworkErrorDialog) {
        NetworkUnavailableDialog(
            text = stringResource(id = R.string.models_missing),
            onTryAgainClick = {
                val jsonContent =
                    readJsonAsMapFromAssets(context, AbbreviationsJsonName)

                onEvent(
                    AddUiEvent.OnTryAgainClick(
                        jsonContent?.get(state.textValue.lowercase()) ?: state.textValue
                    )
                )
            },
            onDismiss = { onEvent(AddUiEvent.OnDismissNetworkErrorDialog) })
    }

    if (state.showLanguageDialog != null) {
        LanguageDialog(
            lazyListContent = state.supportedLanguages,
            translationOption = state.showLanguageDialog,
            filterText = state.languageFilterText,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            onFilterTextChange = { onEvent(AddUiEvent.OnLanguageFilterTextChange(it)) },
            onDismiss = { onEvent(AddUiEvent.OnShowLanguageDialog(null)) },
            onConfirm = {
                onEvent(AddUiEvent.OnConfirmLanguageDialog(it))
            },
            onDownload = { langCode ->
                onEvent(AddUiEvent.OnDownloadLanguage(langCode))
            },
            onRemove = { langCode ->
                onEvent(AddUiEvent.OnRemoveLanguage(langCode))
            }
        )
    }
}

@Composable
fun AddScreen(
    viewModel: AddViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddUiEffect.ShowNetworkErrorMessage -> uiState.snackbarHostState.showSnackbar(
                    context.getString(R.string.network_unavailable)
                )

                AddUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    AddScreenContent(
        state = uiState,
        onEvent = viewModel::uiEventHandler
    )
}

@Preview
@Composable
fun AddScreenPreview() {
    AddScreenContent(
        state = AddState(),
        onEvent = {}
    )
}