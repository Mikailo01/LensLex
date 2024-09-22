package com.bytecause.lenslex.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.ModifiedImagePreviewUiEffect
import com.bytecause.lenslex.ui.events.ModifiedImagePreviewUiEvent
import com.bytecause.lenslex.ui.screens.model.ModifiedImagePreviewState
import com.bytecause.lenslex.ui.screens.viewmodel.ModifiedImagePreviewViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.ehsanmsz.mszprogressindicator.progressindicator.BallGridPulseProgressIndicator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifiedImagePreviewScreenContent(
    state: ModifiedImagePreviewState,
    onEvent: (ModifiedImagePreviewUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                titleRes = R.string.preview,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Gray.copy(alpha = 0.2f),
                    titleContentColor = MaterialTheme.colorScheme.inversePrimary
                )
            ) {
                onEvent(ModifiedImagePreviewUiEvent.OnLaunchCropLauncher)
            }

            AsyncImage(
                model = state.modifiedImageUri,
                contentDescription = stringResource(id = R.string.modified_image_preview),
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                enabled = state.isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.3f)
                ),
                onClick = {
                    onEvent(ModifiedImagePreviewUiEvent.OnProcessImageClick)
                }
            ) {
                Text(text = stringResource(id = R.string.process))
            }

            SnackbarHost(
                hostState = state.snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (state.isProcessing) {
            BallGridPulseProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ModifiedImagePreviewScreen(
    viewModel: ModifiedImagePreviewViewModel = koinViewModel(),
    originalImageUri: Uri,
    modifiedImageUri: Uri,
    onNavigateBack: () -> Unit,
    onClickNavigate: (Screen) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                viewModel.uiEventHandler(ModifiedImagePreviewUiEvent.OnUpdateImage(it))
            }
        } else onNavigateBack()
    }

    LaunchedEffect(key1 = Unit) {
        if (uiState.modifiedImageUri == Uri.EMPTY) viewModel.uiEventHandler(
            ModifiedImagePreviewUiEvent.OnUpdateImage(modifiedImageUri)
        )

        viewModel.effect.collect { effect ->
            when (effect) {
                ModifiedImagePreviewUiEffect.ImageTextless -> uiState.snackbarHostState.showSnackbar(
                    context.getString(R.string.image_does_not_contain_any_text)
                )

                ModifiedImagePreviewUiEffect.LaunchCropLauncher -> {
                    val cropOptions = CropImageContractOptions(originalImageUri, CropImageOptions())
                    imageCropLauncher.launch(cropOptions)
                }

                is ModifiedImagePreviewUiEffect.NavigateWithTextResult -> onClickNavigate(
                    Screen.TextResult(
                        effect.text
                    )
                )
            }
        }
    }

    ModifiedImagePreviewScreenContent(
        state = uiState,
        onEvent = viewModel::uiEventHandler
    )
}

@Composable
@Preview(showBackground = true)
fun ModifiedImageScreenPreview() {
    ModifiedImagePreviewScreenContent(
        state = ModifiedImagePreviewState(),
        onEvent = {}
    )
}