package com.bytecause.lenslex.ui.screens

import android.Manifest
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.bytecause.lenslex.data.local.TTSManager
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.CircularFloatingActionMenu
import com.bytecause.lenslex.ui.components.IntroShowcaseText
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NoteItem
import com.bytecause.lenslex.ui.components.ScrollToTop
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.components.launchPermissionRationaleDialog
import com.bytecause.lenslex.ui.events.HomeUiEffect
import com.bytecause.lenslex.ui.events.HomeUiEvent
import com.bytecause.lenslex.ui.screens.uistate.HomeState
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.util.introShowcaseBackgroundAlpha
import com.bytecause.lenslex.util.isScrollingUp
import com.bytecause.lenslex.util.shimmerEffect
import com.bytecause.lenslex.util.then
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.ehsanmsz.mszprogressindicator.progressindicator.BallGridPulseProgressIndicator
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
    onEvent: (HomeUiEvent) -> Unit
) {
    IntroShowcase(
        showIntroShowCase = state.showIntroShowcase,
        onShowCaseCompleted = { onEvent(HomeUiEvent.OnShowcaseCompleted) }
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
                                .introShowCaseTarget(
                                    index = 0,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    IntroShowcaseText(text = stringResource(id = R.string.tap_to_navigate_into_app_settings))
                                }
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
                            modifier = Modifier
                                .padding(5.dp)
                                .introShowCaseTarget(
                                    index = 1,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    IntroShowcaseText(text = stringResource(id = R.string.language_preferences_showcase_message))
                                },
                            originLangName = state.selectedLanguageOptions.first.lang.langName,
                            targetLangName = state.selectedLanguageOptions.second.lang.langName,
                            isLoading = state.isLoading,
                            onClick = { onEvent(HomeUiEvent.OnShowLanguageDialog(it)) },
                            onSwitchLanguages = { onEvent(HomeUiEvent.OnSwitchLanguages) }
                        )
                        HorizontalDivider(
                            thickness = 3.dp,
                            color = Color.Gray
                        )
                    }
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .introShowCaseTarget(
                            index = 2,
                            style = ShowcaseStyle.Default.copy(
                                backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                backgroundAlpha = introShowcaseBackgroundAlpha,
                                targetCircleColor = Color.White
                            )
                        ) {
                            IntroShowcaseText(text = stringResource(id = R.string.translated_text_list_showcase_message))
                        }) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(8.dp),
                            state = state.lazyListState
                        ) {
                            // shows shimmer effects
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
                                items(
                                    state.wordList.filter { it.languageCode == state.selectedLanguageOptions.first.lang.langCode },
                                    key = { item -> item.timeStamp }) { item ->

                                    item.translations[state.selectedLanguageOptions.second.lang.langCode]?.let {
                                        NoteItem(
                                            originalText = item.word,
                                            translatedText = it,
                                            onRemove = {
                                                onEvent(HomeUiEvent.OnItemRemoved(item))
                                            },
                                            onClick = { text ->
                                                onEvent(
                                                    HomeUiEvent.OnSpeak(
                                                        text = text,
                                                        langCode = if (text == item.word) item.languageCode
                                                        else state.selectedLanguageOptions.second.lang.langCode
                                                    )
                                                )
                                            }
                                        )
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

                    SnackbarHost(hostState = state.snackbarHostState, Modifier.fillMaxWidth())
                }

                AnimatedVisibility(
                    visible = !state.lazyListState.isScrollingUp(),
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

                if (state.showProgressBar) {
                    BallGridPulseProgressIndicator(modifier = Modifier.align(Center))
                }
            }
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

    val ttsManager = remember { TTSManager(context) }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
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
            }
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

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(key1 = uiState.isLoading) {
        // Show intro showcase if necessary only if isLoading == false to avoid intro showcase recompositions
        if (!uiState.isLoading) viewModel.uiEventHandler(HomeUiEvent.OnShowIntroShowcaseIfNecessary)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEventHandler(HomeUiEvent.OnReload)

        viewModel.effect.collect { effect ->
            when (effect) {
                HomeUiEffect.ImageTextless -> uiState.snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.image_does_not_contain_any_text
                    )
                )
            }
        }
    }

    LaunchedEffect(key1 = textResult) {
        if (textResult.isNotEmpty()) onClickNavigate(Screen.TextResult(textResult))
    }

    // Scroll to top of the lazy list if wordList changes
    LaunchedEffect(key1 = uiState.wordList) {
        if (uiState.wordList.isNotEmpty()) {
            uiState.lazyListState.scrollToItem(0)
        }
    }

    HomeScreenContent(
        state = uiState,
        cameraPermissionState = cameraPermissionState,
        onEvent = { event ->
            when (event) {
                HomeUiEvent.OnScrollToTop -> {
                    coroutineScope.launch {
                        uiState.lazyListState.scrollToItem(0)
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

                is HomeUiEvent.OnSpeak -> {
                    ttsManager.speak(
                        text = event.text,
                        langCode = event.langCode
                    ).takeIf { !it }?.let {
                        coroutineScope.launch {
                            uiState.snackbarHostState.showSnackbar(context.getString(R.string.unsupported_language))
                        }
                    }
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
        onEvent = {}
    )
}
