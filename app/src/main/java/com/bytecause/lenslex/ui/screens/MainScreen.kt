package com.bytecause.lenslex.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.ComposeFileProvider
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.mlkit.TextRecognizer
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.CircularFloatingActionMenu
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NoteItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.components.launchPermissionRationaleDialog
import com.bytecause.lenslex.ui.screens.viewmodel.MainViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

enum class FabNavigation { CAMERA, GALLERY, ADD }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    sharedViewModel: TextRecognitionSharedViewModel,
    onClickNavigate: (NavigationItem) -> Unit,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current

    val selectedLanguage by viewModel.languageOptionFlow.collectAsStateWithLifecycle(
        SupportedLanguage()
    )

    val wordList by viewModel.getAllWords.collectAsStateWithLifecycle(initialValue = emptyList())

    var deletedItemsStack by remember {
        mutableStateOf<List<WordAndSentenceEntity>>(emptyList())
    }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the cropped image
            result.uriContent?.let(onPhotoTaken)
        } else {
            // an error occurred cropping
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val cropOptions = CropImageContractOptions(imageUri, CropImageOptions())
                imageCropLauncher.launch(cropOptions)
            } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT).show()
        }
    )

    val cameraPermissionState =
        rememberPermissionState(Manifest.permission.CAMERA) { isGranted ->
            if (isGranted) {
                val uri = ComposeFileProvider.getImageUri(context = context)
                imageUri = uri

                cameraLauncher.launch(uri)
            }
        }

    var showProgressBar by remember {
        mutableStateOf(false)
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isEmpty()) return@rememberLauncherForActivityResult
            else if (uris.size == 1) {
                val cropOptions = CropImageContractOptions(uris.first(), CropImageOptions())
                imageCropLauncher.launch(cropOptions)
                return@rememberLauncherForActivityResult
            }

            showProgressBar = true

            TextRecognizer(context).runTextRecognition(imagePaths = uris) {
                showProgressBar = false
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

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.app_name,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = Icons.Filled.Menu,
                actionIcon = {
                    if (deletedItemsStack.isNotEmpty()) {
                        Image(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.insertOrUpdateWordAndSentenceEntity(deletedItemsStack.last())
                                    deletedItemsStack = deletedItemsStack
                                        .toMutableList()
                                        .filter { it.id != deletedItemsStack.last().id }
                                },
                            painter = painterResource(id = R.drawable.baseline_undo_24),
                            contentDescription = "Undo remove"
                        )
                    }
                },
                onNavigationIconClick = { /* TODO() */ }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = CenterHorizontally
                ) {
                    LanguagePreferences(
                        modifier = Modifier.padding(5.dp),
                        text = selectedLanguage.langName,
                        onClick = { viewModel.onSelectLanguageClick() }
                    )
                    Divider(
                        thickness = 3,
                        color = Color.Gray
                    )
                }
                LazyColumn(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(wordList, key = { item -> item.id }) { item ->
                        item.translations[selectedLanguage.langCode]?.let {
                            NoteItem(
                                originalText = item.words,
                                translatedText = it
                            ) {
                                deletedItemsStack = deletedItemsStack + item
                                viewModel.deleteWordById(item.id)
                            }
                        }
                    }
                }
            }

            CircularFloatingActionMenu(
                fabColor = MaterialTheme.colorScheme.primary,
                fabContentColor = MaterialTheme.colorScheme.onPrimary,
                expandedFabBackgroundColor = MaterialTheme.colorScheme.inversePrimary,
                onInnerContentClick = { fabNavigation ->

                    when (fabNavigation) {
                        FabNavigation.CAMERA -> {
                            when (cameraPermissionState.status) {
                                PermissionStatus.Granted -> {
                                    val uri = ComposeFileProvider.getImageUri(context = context)
                                    imageUri = uri

                                    cameraLauncher.launch(uri)
                                }

                                PermissionStatus.Denied(true) -> launchPermissionRationaleDialog(
                                    context = context
                                )

                                else -> {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        }

                        FabNavigation.GALLERY -> {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }

                        FabNavigation.ADD -> {
                            onClickNavigate(NavigationItem.Add)
                        }
                    }
                }
            )

            if (viewModel.setShowLanguageDialog) {
                LanguageDialog(
                    lazyListContent = viewModel.supportedLanguages,
                    onDismiss = { viewModel.onDismissDialog() },
                    onConfirm = { language ->
                        viewModel.saveTranslationOption(language)
                        viewModel.onDismissDialog()
                    },
                    onDownload = {
                        // TODO()
                    }
                )
            }

            IndeterminateCircularIndicator(
                modifier = Modifier.align(Center),
                isShowed = showProgressBar
            )
        }
    }
}
