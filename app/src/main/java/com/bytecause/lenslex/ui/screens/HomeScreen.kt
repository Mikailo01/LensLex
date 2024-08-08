package com.bytecause.lenslex.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.ComposeFileProvider
import com.bytecause.lenslex.data.local.TTSManager
import com.bytecause.lenslex.domain.models.Words
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.CircularFloatingActionMenu
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.IntroShowcaseContent
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
import com.bytecause.lenslex.util.PulsingAnimation
import com.bytecause.lenslex.util.introShowcaseBackgroundAlpha
import com.bytecause.lenslex.util.isScrollingUp
import com.bytecause.lenslex.util.shimmerEffect
import com.bytecause.lenslex.util.then
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.ehsanmsz.mszprogressindicator.progressindicator.BallGridPulseProgressIndicator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

enum class FabNavigation { CAMERA, GALLERY, ADD }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    isExpandedScreen: Boolean,
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
                        if (state.deletedItemsStack.isNotEmpty()) {
                            Image(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onEvent(HomeUiEvent.OnItemRestored)
                                    }
                                    .introShowCaseTarget(
                                        index = 8,
                                        style = ShowcaseStyle.Default.copy(
                                            backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                            backgroundAlpha = introShowcaseBackgroundAlpha,
                                            targetCircleColor = Color.White
                                        )
                                    ) {
                                        IntroShowcaseContent(
                                            iconRes = R.drawable.undo,
                                            messageRes = R.string.restore_deleted_item_message
                                        )
                                    },
                                painter = painterResource(id = R.drawable.baseline_undo_24),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                contentDescription = stringResource(id = R.string.undo_changes)
                            )
                        }

                        if (state.wordList.isNotEmpty() || state.deletedItemsStack.isNotEmpty()) {
                            IconButton(
                                onClick = { onEvent(HomeUiEvent.OnEditStateChange(!state.isEditEnabled)) },
                                modifier = Modifier
                                    .introShowCaseTarget(
                                        index = 6,
                                        style = ShowcaseStyle.Default.copy(
                                            backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                            backgroundAlpha = introShowcaseBackgroundAlpha,
                                            targetCircleColor = Color.White
                                        )
                                    ) {
                                        IntroShowcaseContent(
                                            iconRes = R.drawable.edit,
                                            messageRes = R.string.enter_edit_mode_message
                                        )
                                    }
                            ) {
                                if (state.isEditEnabled) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.inversePrimary,
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = stringResource(id = R.string.exit_edit_mode),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(5.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = stringResource(id = R.string.enable_edit_mode),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }

                        // user's profile picture and button for navigating into app and account settings
                        AsyncImage(
                            model = state.profilePictureUrl.takeIf { it != "null" }
                                ?: R.drawable.default_account_image,
                            contentDescription = stringResource(id = R.string.app_settings),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
                                .clickable {
                                    onEvent(HomeUiEvent.OnNavigate(Screen.Account))
                                }
                                .then(state.isLoading, onTrue = { shimmerEffect() })
                                .introShowCaseTarget(
                                    index = 5,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    IntroShowcaseContent(
                                        iconRes = R.drawable.settings,
                                        messageRes = R.string.tap_to_navigate_into_app_settings
                                    )
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
                                    index = 0,
                                    style = ShowcaseStyle.Default.copy(
                                        backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                        backgroundAlpha = introShowcaseBackgroundAlpha,
                                        targetCircleColor = Color.White
                                    )
                                ) {
                                    IntroShowcaseContent(
                                        iconRes = R.drawable.language,
                                        messageRes = R.string.language_preferences_showcase_message
                                    )
                                },
                            originLangName = state.selectedLanguageOptions.first.lang.langName,
                            targetLangName = state.selectedLanguageOptions.second.lang.langName,
                            onClick = { onEvent(HomeUiEvent.OnShowLanguageDialog(it)) },
                            onSwitchLanguages = { onEvent(HomeUiEvent.OnSwitchLanguages) }
                        )
                        HorizontalDivider(
                            thickness = 3.dp,
                            color = Color.Gray
                        )
                    }

                    var scale by remember {
                        mutableFloatStateOf(1f)
                    }

                    // start scale animation if edit mode is enabled
                    if (state.isEditEnabled) {
                        PulsingAnimation {
                            scale = it
                        }
                    } else scale = 1f

                    if (isExpandedScreen) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            state = state.lazyGridState
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
                                itemsIndexed(
                                    state.wordList,
                                    key = { _, item -> item.timeStamp }) { index, item ->
                                    ListContent(
                                        state = state,
                                        item = item,
                                        index = index,
                                        scale = scale,
                                        onEvent = onEvent
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                itemsIndexed(
                                    state.wordList,
                                    key = { _, item -> item.timeStamp }) { index, item ->
                                    ListContent(
                                        state = state,
                                        item = item,
                                        index = index,
                                        scale = scale,
                                        onEvent = onEvent
                                    )
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
                    visible = if (isExpandedScreen) !state.lazyGridState.isScrollingUp()
                    else !state.lazyListState.isScrollingUp(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ScrollToTop {
                        onEvent(HomeUiEvent.OnScrollToTop)
                    }
                }

                if (state.showDeleteConfirmationDialog) {
                    ConfirmationDialog(
                        onDismiss = {
                            onEvent(
                                HomeUiEvent.OnDeleteConfirmationDialogResult(
                                    false
                                )
                            )
                        },
                        onConfirm = { onEvent(HomeUiEvent.OnDeleteConfirmationDialogResult(true)) }) {
                        Text(
                            text = stringResource(id = R.string.permanent_deletion_dialog_message),
                            textAlign = TextAlign.Center
                        )
                    }
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
                        onFilterTextChange = { onEvent(HomeUiEvent.OnLanguageFilterTextChange(it)) },
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

@Composable
private fun IntroShowcaseScope.ListContent(
    state: HomeState,
    item: Words,
    index: Int,
    scale: Float,
    onEvent: (HomeUiEvent) -> Unit
) {
    // get translated text for currently chosen target lang option
    item.translations[state.selectedLanguageOptions.second.lang.langCode]?.let {
        NoteItem(
            showIntro = index == 0,
            originalText = item.word,
            translatedText = it,
            isEditEnabled = state.isEditEnabled,
            scale = scale,
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
            },
            // set focus of introShowCase on first element in the list
            modifier = Modifier.then(index == 0, onTrue = {
                introShowCaseTarget(
                    index = 7,
                    style = ShowcaseStyle.Default.copy(
                        backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                        backgroundAlpha = introShowcaseBackgroundAlpha,
                        targetCircleColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            15.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.swipe_left),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(64.dp)
                                .weight(0.5f)
                        )
                        Text(
                            text = stringResource(id = R.string.swipe_left_or_right_message),
                            modifier = Modifier
                                .weight(2f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.swipe_right),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(64.dp)
                                .weight(0.5f)
                        )
                    }
                }
            })
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    isExpandedScreen: Boolean,
    onClickNavigate: (Screen) -> Unit,
    onPhotoTaken: (Uri, Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    // Text to speech manager must be properly closed when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    // If lang options change, item is removed or restored, fetch new item list
    LaunchedEffect(
        key1 = uiState.selectedLanguageOptions,
        key2 = uiState.deletedItemsStack
    ) {
        // don't fetch real item list if showIntroShowcase is running
        if (uiState.showIntroShowcase) return@LaunchedEffect

        viewModel.uiEventHandler(HomeUiEvent.OnFetchItemList)
    }

    LaunchedEffect(key1 = Unit) {
        if (uiState.supportedLanguages.isNotEmpty()) {
            if (uiState.supportedLanguages.first().langName != Locale(uiState.supportedLanguages.first().langCode).displayName) {
                viewModel.uiEventHandler(HomeUiEvent.OnUpdateSupportedLanguages)
            }
        }

        // Launch another coroutine to avoid blocking entire LaunchedEffect block
        launch {
            // Wait until isLoading == false to avoid unnecessary intro showcase recompositions
            while (uiState.isLoading) {
                delay(500)
            }
            viewModel.uiEventHandler(HomeUiEvent.OnShowIntroShowcaseIfNecessary)
        }

        viewModel.uiEventHandler(HomeUiEvent.OnReload)

        viewModel.effect.collect { effect ->
            when (effect) {
                HomeUiEffect.ImageTextless -> uiState.snackbarHostState.showSnackbar(
                    context.getString(
                        R.string.image_does_not_contain_any_text
                    )
                )

                HomeUiEffect.CameraIntentLaunch -> {
                    val uri =
                        ComposeFileProvider.getImageUri(context = context)

                    imageUri = uri
                    cameraLauncher.launch(uri)
                }

                HomeUiEffect.MultiplePhotoPickerLaunch -> {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                HomeUiEffect.PermissionDialogLaunch -> launchPermissionRationaleDialog(context = context)
                HomeUiEffect.ScrollToTop -> {
                    if (isExpandedScreen) {
                        uiState.lazyGridState.scrollToItem(0)
                    } else uiState.lazyListState.scrollToItem(0)
                }

                is HomeUiEffect.NavigateTo -> onClickNavigate(effect.destination)
                is HomeUiEffect.Speak -> {
                    ttsManager.speak(
                        text = effect.text,
                        langCode = effect.langCode
                    ).takeIf { !it }?.let {
                        coroutineScope.launch {
                            uiState.snackbarHostState.showSnackbar(context.getString(R.string.unsupported_language))
                        }
                    }
                }

                is HomeUiEffect.TextResult -> onClickNavigate(Screen.TextResult(effect.text))
            }
        }
    }

    // Scroll to top of the lazy list if wordList changes
    LaunchedEffect(key1 = uiState.wordList) {
        uiState.lazyListState.scrollToItem(0)
    }

    HomeScreenContent(
        state = uiState,
        isExpandedScreen = isExpandedScreen,
        cameraPermissionState = cameraPermissionState,
        onEvent = viewModel::uiEventHandler
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreenContent(
        state = HomeState(),
        isExpandedScreen = false,
        cameraPermissionState = null,
        onEvent = {}
    )
}
