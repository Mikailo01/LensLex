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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.ComposeFileProvider
import com.bytecause.lenslex.mlkit.TextRecognizer
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.CircularFloatingActionMenu
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NoteItem
import com.bytecause.lenslex.ui.components.ScrollToTop
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.components.launchPermissionRationaleDialog
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel
import com.bytecause.lenslex.util.isScrollingUp
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
    wordList: List<WordsAndSentences>,
    profilePictureUrl: String,
    fabState: Boolean,
    showProgressBar: Boolean,
    cameraPermissionState: PermissionState?,
    supportedLanguages: List<SupportedLanguage>,
    selectedLanguage: SupportedLanguage,
    showLanguageDialog: Boolean,
    deletedItemsStack: List<WordsAndSentences>,
    onIconStateChanged: (Boolean) -> Unit,
    onConfirmLanguageDialog: (SupportedLanguage) -> Unit,
    onShowLanguageDialog: (Boolean) -> Unit,
    onClickNavigate: (NavigationItem) -> Unit,
    onCameraIntent: (Uri) -> Unit,
    onMultiplePhotoPickerLaunch: () -> Unit,
    onDownloadLanguage: (String) -> Unit,
    onRemoveLanguage: (String) -> Unit,
    onItemRemoved: (WordsAndSentences) -> Unit,
    onItemRestored: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.app_name,
                actionIcon = {
                    if (deletedItemsStack.isNotEmpty()) {
                        Image(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onItemRestored()
                                },
                            painter = painterResource(id = R.drawable.baseline_undo_24),
                            contentDescription = "Undo remove"
                        )
                    }

                    AsyncImage(
                        model = profilePictureUrl.takeIf { it != "null" }
                            ?: R.drawable.default_account_image,
                        contentDescription = "avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                onClickNavigate(NavigationItem.SettingsGraph)
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

            val lazyListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = CenterHorizontally
                ) {
                    LanguagePreferences(
                        modifier = Modifier.padding(5.dp),
                        text = selectedLanguage.langName,
                        onClick = { onShowLanguageDialog(true) }
                    )
                    Divider(
                        thickness = 3,
                        color = Color.Gray
                    )
                }

                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    state = lazyListState
                ) {
                    items(wordList, key = { item -> item.timeStamp }) { item ->
                        item.translations[selectedLanguage.langCode]?.let {
                            NoteItem(
                                originalText = item.word,
                                translatedText = it
                            ) {
                                onItemRemoved(item)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !lazyListState.isScrollingUp(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ScrollToTop {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(0)
                    }
                }
            }

            CircularFloatingActionMenu(
                iconState = fabState,
                fabColor = MaterialTheme.colorScheme.primary,
                fabContentColor = MaterialTheme.colorScheme.onPrimary,
                expandedFabBackgroundColor = MaterialTheme.colorScheme.inversePrimary,
                onInnerContentClick = { fabNavigation ->

                    when (fabNavigation) {
                        FabNavigation.CAMERA -> {
                            when (cameraPermissionState?.status) {
                                PermissionStatus.Granted -> {
                                    val uri = ComposeFileProvider.getImageUri(context = context)
                                    onCameraIntent(uri)
                                }

                                PermissionStatus.Denied(true) -> launchPermissionRationaleDialog(
                                    context = context
                                )

                                else -> {
                                    cameraPermissionState?.launchPermissionRequest()
                                }
                            }
                        }

                        FabNavigation.GALLERY -> {
                            onMultiplePhotoPickerLaunch()
                        }

                        FabNavigation.ADD -> {
                            onClickNavigate(NavigationItem.Add)
                        }
                    }
                },
                onIconStateChange = {
                    onIconStateChanged(it)
                }
            )

            if (showLanguageDialog) {
                LanguageDialog(
                    lazyListContent = supportedLanguages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp),
                    onDismiss = { onShowLanguageDialog(false) },
                    onConfirm = { language ->
                        onConfirmLanguageDialog(language)
                    },
                    onDownload = { langCode ->
                        onDownloadLanguage(langCode)
                    },
                    onRemove = { langCode ->
                        onRemoveLanguage(langCode)
                    }
                )
            }

            IndeterminateCircularIndicator(
                modifier = Modifier.align(Center),
                size = 65.dp,
                isShowed = showProgressBar,
                subContent = { Text(text = "Processing") }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    sharedViewModel: TextRecognitionSharedViewModel,
    onClickNavigate: (NavigationItem) -> Unit,
    onPhotoTaken: (Uri, Uri) -> Unit
) {

    val context = LocalContext.current

    val supportedLanguages by viewModel.supportedLanguages.collectAsStateWithLifecycle()
    val getSignedInUser by viewModel.getSignedInUser.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.languageOptionFlow.collectAsStateWithLifecycle(
        SupportedLanguage()
    )

    val wordList by viewModel.getAllWordsFromFireStore.collectAsStateWithLifecycle(initialValue = emptyList())

    val deletedItemsStack by viewModel.deletedItemsStack.collectAsStateWithLifecycle()

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var showLanguageDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var fabState by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    HomeScreenContent(
        wordList = wordList,
        profilePictureUrl = getSignedInUser?.profilePictureUrl.toString(),
        fabState = fabState,
        showProgressBar = showProgressBar,
        cameraPermissionState = cameraPermissionState,
        supportedLanguages = supportedLanguages,
        selectedLanguage = selectedLanguage,
        showLanguageDialog = showLanguageDialog,
        deletedItemsStack = deletedItemsStack,
        onIconStateChanged = {
            fabState = it
        },
        onConfirmLanguageDialog = { language ->
            viewModel.saveTranslationOption(language)
            showLanguageDialog = false
        },
        onShowLanguageDialog = {
            showLanguageDialog = it
        },
        onClickNavigate = {
            onClickNavigate(it)
        },
        onCameraIntent = { uri ->
            imageUri = uri
            cameraLauncher.launch(uri)
        },
        onMultiplePhotoPickerLaunch = {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onDownloadLanguage = { langCode ->
            viewModel.downloadModel(langCode)
        },
        onRemoveLanguage = { langCode ->
            viewModel.removeModel(langCode)
        },
        onItemRemoved = { item ->
            viewModel.addDeletedItemToStack(item)
            viewModel.deleteWordFromFireStore(item.id)
        },
        onItemRestored = {
            viewModel.insertWordToFireStore(deletedItemsStack.last())
            viewModel.removeDeletedItemFromStack()
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun HomeScreenPreview() {
    HomeScreenContent(
        wordList = emptyList(),
        profilePictureUrl = "",
        fabState = true,
        showProgressBar = false,
        cameraPermissionState = null,
        supportedLanguages = emptyList(),
        selectedLanguage = SupportedLanguage("cs", "Czech"),
        showLanguageDialog = false,
        deletedItemsStack = emptyList(),
        onIconStateChanged = {},
        onConfirmLanguageDialog = {},
        onShowLanguageDialog = {},
        onClickNavigate = {},
        onCameraIntent = {},
        onMultiplePhotoPickerLaunch = {},
        onDownloadLanguage = {},
        onRemoveLanguage = {},
        onItemRemoved = {},
        onItemRestored = {}
    )
}
