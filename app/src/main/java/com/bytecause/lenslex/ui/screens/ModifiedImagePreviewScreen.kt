package com.bytecause.lenslex.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.mlkit.TextRecognizer
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifiedImagePreviewScreenContent(
    originalImageUri: Uri,
    modifiedImageUri: Uri,
    onClickNavigate: (NavigationItem) -> Unit,
    onProcessedTextUpdate: (List<String>) -> Unit
) {
    val context = LocalContext.current

    var isProgressBarShown by remember {
        mutableStateOf(false)
    }

    var uri by rememberSaveable {
        mutableStateOf(modifiedImageUri)
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let {
                uri = it
            }
        } else {
            // an error occurred cropping
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
    ) {

        TopAppBar(
            titleRes = R.string.preview,
            navigationIcon = Icons.Filled.ArrowBack,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Gray.copy(alpha = 0.1f)
            )
        ) {
            val cropOptions = CropImageContractOptions(originalImageUri, CropImageOptions())
            imageCropLauncher.launch(cropOptions)
        }

        AsyncImage(
            model = uri,
            contentDescription = stringResource(id = R.string.modified_image_preview),
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            onClick = {
                isProgressBarShown = true
                TextRecognizer(context).runTextRecognition(listOf(uri)) {
                    isProgressBarShown = false
                    if (it.isEmpty()) {
                        Toast.makeText(
                            context,
                            "This image doesn't contain any text.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@runTextRecognition
                    }
                    onProcessedTextUpdate(it)
                    onClickNavigate(NavigationItem.TextResult)
                }
            }
        ) {
            Text(text = "Process")
        }
        IndeterminateCircularIndicator(
            modifier = Modifier.align(Alignment.Center),
            size = 65.dp,
            isShowed = isProgressBarShown,
            subContent = { Text(text = "Processing") }
        )
    }
}

@Composable
fun ModifiedImagePreviewScreen(
    sharedViewModel: TextRecognitionSharedViewModel,
    originalImageUri: Uri,
    modifiedImageUri: Uri,
    onClickNavigate: (NavigationItem) -> Unit
) {

    ModifiedImagePreviewScreenContent(
        originalImageUri = originalImageUri,
        modifiedImageUri = modifiedImageUri,
        onClickNavigate = {
            onClickNavigate(it)
        },
        onProcessedTextUpdate = {
            sharedViewModel.updateProcessedTextState(it)
        }
    )
}

@Composable
@Preview(showBackground = true)
fun ModifiedImageScreenPreview() {
    ModifiedImagePreviewScreenContent(
        originalImageUri = Uri.EMPTY,
        modifiedImageUri = Uri.EMPTY,
        onClickNavigate = {},
        onProcessedTextUpdate = {}
    )
}