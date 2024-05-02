package com.bytecause.lenslex.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    uri: Uri,
    isProcessing: Boolean,
    onLaunchCropLauncher: () -> Unit,
    onProcessImageClick: () -> Unit,
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
                onLaunchCropLauncher()
            }

            AsyncImage(
                model = uri,
                contentDescription = stringResource(id = R.string.modified_image_preview),
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            onClick = {
                onProcessImageClick()
            }
        ) {
            Text(text = stringResource(id = R.string.process))
        }
        IndeterminateCircularIndicator(
            modifier = Modifier.align(Alignment.Center),
            size = 65.dp,
            isShowed = isProcessing,
            subContent = { Text(text = stringResource(id = R.string.processing)) }
        )
    }
}

@Composable
fun ModifiedImagePreviewScreen(
    sharedViewModel: TextRecognitionSharedViewModel,
    originalImageUri: Uri,
    modifiedImageUri: Uri,
    onNavigateBack: () -> Unit,
    onClickNavigate: (NavigationItem) -> Unit
) {
    val context = LocalContext.current

    var isProcessing by remember {
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
            onNavigateBack()
        }
    }

    ModifiedImagePreviewScreenContent(
        uri = uri,
        isProcessing = isProcessing,
        onLaunchCropLauncher = {
            val cropOptions = CropImageContractOptions(originalImageUri, CropImageOptions())
            imageCropLauncher.launch(cropOptions)
        },
        onProcessImageClick = {
            isProcessing = true
            TextRecognizer(context).runTextRecognition(listOf(uri)) {
                isProcessing = false

                if (it.isEmpty()) {
                    Toast.makeText(
                        context,
                        "This image doesn't contain any text.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runTextRecognition
                }

                sharedViewModel.updateProcessedTextState(it)
                onClickNavigate(NavigationItem.TextResult)
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun ModifiedImageScreenPreview() {
    ModifiedImagePreviewScreenContent(
        uri = Uri.EMPTY,
        isProcessing = false,
        onLaunchCropLauncher = {},
        onProcessImageClick = {}
    )
}