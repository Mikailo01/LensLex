package com.bytecause.lenslex.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.ComposeFileProvider
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.CircularFloatingActionMenu
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NoteItem
import com.bytecause.lenslex.ui.components.ScrollToTop
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.components.launchPermissionRationaleDialog
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.util.isScrollingUp
import com.bytecause.lenslex.util.shimmerEffect
import com.bytecause.lenslex.util.then
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

enum class FabNavigation { CAMERA, GALLERY, ADD }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    cameraPermissionState: PermissionState?,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onEvent: (HomeUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.app_name,
                actionIcons = listOf {
                    if (state.showUndoButton) {
                        Image(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onEvent(HomeUiEvent.OnItemRestored)
                                },
                            painter = painterResource(id = R.drawable.baseline_undo_24),
                            contentDescription = stringResource(id = R.string.undo_changes)
                        )
                    }

                    AsyncImage(
                        model = state.profilePictureUrl.takeIf { it != "null" }
                            ?: R.drawable.default_account_image,
                        contentDescription = "avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                onEvent(HomeUiEvent.OnNavigate(Screen.Account))
                            }
                            .then(state.isLoading, onTrue = { shimmerEffect() })
                    )
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = CenterHorizontally
                ) {
                    LanguagePreferences(
                        modifier = Modifier.padding(5.dp),
                        originLangName = state.selectedLanguageOptions.first.lang.langName,
                        targetLangName = state.selectedLanguageOptions.second.lang.langName,
                        isLoading = state.isLoading,
                        onClick = { onEvent(HomeUiEvent.OnShowLanguageDialog(it)) }
                    )
                    HorizontalDivider(
                        thickness = 3.dp,
                        color = Color.Gray
                    )
                }
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    state = lazyListState
                ) {
                    if (state.isLoading) {
                        items(10) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .shimmerEffect()
                            ) {}
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        if (state.wordList.none { it.languageCode == state.selectedLanguageOptions.first.lang.langCode }) return@LazyColumn

                        items(state.wordList, key = { item -> item.timeStamp }) { item ->
                            item.translations[state.selectedLanguageOptions.second.lang.langCode]?.let {
                                NoteItem(
                                    originalText = item.word,
                                    translatedText = it
                                ) {
                                    onEvent(HomeUiEvent.OnItemRemoved(item))
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Column(modifier = Modifier.wrapContentSize()) {
                    CircularFloatingActionMenu(
                        iconState = state.fabState,
                        fabColor = MaterialTheme.colorScheme.primary,
                        fabContentColor = MaterialTheme.colorScheme.onPrimary,
                        expandedFabBackgroundColor = MaterialTheme.colorScheme.inversePrimary,
                        onInnerContentClick = { fabNavigation ->
                            when (fabNavigation) {
                                FabNavigation.CAMERA -> {
                                    when (cameraPermissionState?.status) {
                                        PermissionStatus.Granted -> {
                                            onEvent(HomeUiEvent.OnCameraIntentLaunch)
                                        }

                                        PermissionStatus.Denied(true) -> onEvent(HomeUiEvent.OnPermissionDialogLaunch)

                                        else -> {
                                            cameraPermissionState?.launchPermissionRequest()
                                        }
                                    }
                                }

                                FabNavigation.GALLERY -> onEvent(HomeUiEvent.OnMultiplePhotoPickerLaunch)

                                FabNavigation.ADD -> onEvent(HomeUiEvent.OnNavigate(Screen.Add))
                            }
                        },
                        onIconStateChange = {
                            onEvent(HomeUiEvent.OnIconStateChange(it))
                        }
                    )
                }

                SnackbarHost(hostState = snackbarHostState, Modifier.fillMaxWidth())
            }

            AnimatedVisibility(
                visible = !lazyListState.isScrollingUp(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ScrollToTop {
                    onEvent(HomeUiEvent.OnScrollToTop)
                }
            }

            if (state.showLanguageDialog != null) {
                LanguageDialog(
                    lazyListContent = state.supportedLanguages,
                    translationOption = state.showLanguageDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp),
                    onDismiss = { onEvent(HomeUiEvent.OnShowLanguageDialog(null)) },
                    onConfirm = { language ->
                        onEvent(HomeUiEvent.OnConfirmLanguageDialog(language))
                    },
                    onDownload = { langCode ->
                        onEvent(HomeUiEvent.OnDownloadLanguage(langCode))
                    },
                    onRemove = { langCode ->
                        onEvent(HomeUiEvent.OnRemoveLanguage(langCode))
                    }
                )
            }

            IndeterminateCircularIndicator(
                modifier = Modifier.align(Center),
                size = 65.dp,
                isShowed = state.showProgressBar,
                subContent = { Text(text = stringResource(id = R.string.processing)) }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onClickNavigate: (Screen) -> Unit,
    onPhotoTaken: (Uri, Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textResult by viewModel.textResultChannel.collectAsStateWithLifecycle(initialValue = emptyList())

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the cropped image

            val originalUri = result.originalUri
            val modifiedUri = result.uriContent

            if (originalUri != null && modifiedUri != null) {
                onPhotoTaken(originalUri, modifiedUri)
            }

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

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isEmpty()) return@rememberLauncherForActivityResult
            else if (uris.size == 1) {
                val cropOptions = CropImageContractOptions(uris.first(), CropImageOptions())
                imageCropLauncher.launch(cropOptions)
                return@rememberLauncherForActivityResult
            }

            viewModel.uiEventHandler(HomeUiEvent.OnTextRecognition(uris))
        }
    )

    LaunchedEffect(key1 = uiState.isImageTextless) {
        if (uiState.isImageTextless) {
            snackbarHostState.showSnackbar(context.getString(R.string.image_does_not_contain_any_text))
            viewModel.resetImageTextless()
        }
    }

    LaunchedEffect(key1 = textResult) {
        if (textResult.isNotEmpty()) onClickNavigate(Screen.TextResult(textResult))
    }

    HomeScreenContent(
        state = uiState,
        cameraPermissionState = cameraPermissionState,
        lazyListState = lazyListState,
        snackbarHostState = snackbarHostState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.OnScrollToTop -> {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }

                is HomeUiEvent.OnNavigate -> {
                    onClickNavigate(event.destination)
                }

                is HomeUiEvent.OnCameraIntentLaunch -> {
                    val uri =
                        ComposeFileProvider.getImageUri(context = context)

                    imageUri = uri
                    cameraLauncher.launch(uri)
                }

                HomeUiEvent.OnMultiplePhotoPickerLaunch -> {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                HomeUiEvent.OnPermissionDialogLaunch -> launchPermissionRationaleDialog(context = context)

                else -> viewModel.uiEventHandler(event as HomeUiEvent.NonDirect)
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreenContent(
        state = HomeState(),
        cameraPermissionState = null,
        lazyListState = rememberLazyListState(),
        snackbarHostState = remember {
            SnackbarHostState()
        },
        onEvent = {}
    )
}
