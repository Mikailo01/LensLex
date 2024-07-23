package com.bytecause.lenslex.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bytecause.lenslex.R
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.AppLanguageRow
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.ProfilePicture
import com.bytecause.lenslex.ui.components.RowItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.AccountUiEvent
import com.bytecause.lenslex.ui.screens.uistate.AccountState
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.util.BlurTransformation
import com.bytecause.lenslex.util.compressImage
import com.bytecause.lenslex.util.shimmerEffect
import com.bytecause.lenslex.util.then
import org.koin.androidx.compose.koinViewModel
import java.io.InputStream
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreenContent(
    state: AccountState,
    bottomSheetState: SheetState,
    onEvent: (AccountUiEvent) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            ) {
                onEvent(AccountUiEvent.OnBackButtonClick)
            }
        }
    ) { innerPaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .height(IntrinsicSize.Max)
                .padding(innerPaddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .blur(3.dp), // Ignored if Android API level < 31
            ) {
                // Blurred background
                Column(modifier = Modifier.fillMaxSize()) {
                    // AsyncImage wrapped in Box
                    Box(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                ImageRequest.Builder(context)
                                    .data(state.userData?.profilePictureUrl.takeIf { it != "null" }
                                        ?: R.drawable.default_account_image)
                                    .transformations(BlurTransformation(context, 15))
                                    .build()
                            } else state.userData?.profilePictureUrl.takeIf { it != "null" }
                                ?: R.drawable.default_account_image,
                            contentDescription = null,
                            onState = {
                                it.painter?.let {
                                    onEvent(
                                        AccountUiEvent.OnImageLoading(
                                            false
                                        )
                                    )
                                } ?: onEvent(AccountUiEvent.OnImageLoading(true))
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(state.isImageLoading, onTrue = { shimmerEffect() })
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPaddingValues)
                    .padding(top = 120.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.userData?.userName?.isNotBlank() == true) {
                        Text(
                            text = state.userData.userName,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {

                    RowItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconId = R.drawable.baseline_language_24,
                        contentDescription = R.string.select_language,
                        text = R.string.language
                    ) {
                        onEvent(AccountUiEvent.OnShowLanguageDialog(true))
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                    RowItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconId = R.drawable.baseline_manage_accounts_24,
                        contentDescription = R.string.account_settings,
                        text = R.string.account_settings
                    ) {
                        onEvent(AccountUiEvent.OnNavigate(Screen.AccountSettings))
                    }

                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                    RowItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconId = R.drawable.baseline_logout_24,
                        contentDescription = R.string.sign_out,
                        text = R.string.sign_out
                    ) {
                        if (state.userData?.isAnonymous == true) onEvent(
                            AccountUiEvent.OnShowConfirmationDialog(
                                true
                            )
                        )
                        else onEvent(AccountUiEvent.OnSignOut)
                    }
                }
            }

            ProfilePicture(
                profilePicture = state.userData?.profilePictureUrl.takeIf { it != "" }.toString(),
                isLoading = state.isImageLoading,
                modifier = Modifier.padding(top = 130.dp)
            ) {
                onEvent(AccountUiEvent.OnShowBottomSheet(true))
            }

            if (state.showLanguageDialog) {
                val getLocale = AppCompatDelegate.getApplicationLocales()[0]?.isO3Language

                Dialog(
                    title = stringResource(id = R.string.choose_language),
                    onDismiss = { onEvent(AccountUiEvent.OnShowLanguageDialog(false)) }
                ) {

                    AppLanguageRow(
                        langCode = "eng",
                        isChecked = getLocale?.let {
                            it == "eng"
                        } ?: (Locale.getDefault().isO3Language == "eng")
                    ) {
                        onEvent(AccountUiEvent.OnShowLanguageDialog(false))
                        onEvent(AccountUiEvent.OnChangeFirebaseLanguage("en"))

                        val appLocale: LocaleListCompat =
                            LocaleListCompat.forLanguageTags("en-EN")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }

                    AppLanguageRow(
                        langCode = "cze",
                        isChecked = getLocale?.let {
                            it == "ces"
                        } ?: (Locale.getDefault().isO3Language == "ces")
                    ) {
                        onEvent(AccountUiEvent.OnShowLanguageDialog(false))
                        onEvent(AccountUiEvent.OnChangeFirebaseLanguage("cs"))

                        val appLocale: LocaleListCompat =
                            LocaleListCompat.forLanguageTags("cs-CZ")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            }
        }

        if (state.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { onEvent(AccountUiEvent.OnShowBottomSheet(false)) },
                sheetState = bottomSheetState,
            ) {

                RowItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingIconId = R.drawable.baseline_image_24,
                    contentDescription = R.string.pick_profile_picture_from_storage,
                    text = R.string.pick_profile_picture_from_storage
                ) {
                    onEvent(AccountUiEvent.OnShowBottomSheet(false))
                    onEvent(AccountUiEvent.OnSinglePicturePickerLaunch)
                }

                RowItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingIconId = R.drawable.baseline_link_24,
                    contentDescription = R.string.set_profile_picture_from_url,
                    text = R.string.set_profile_picture_from_url
                ) {
                    onEvent(AccountUiEvent.OnShowBottomSheet(false))
                    onEvent(AccountUiEvent.OnShowUrlDialog(true))
                }
            }
        }
    }

    if (state.showUrlDialog) {
        Dialog(
            title = stringResource(id = R.string.type_picture_url),
            onDismiss = { onEvent(AccountUiEvent.OnShowUrlDialog(false)) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.urlValue,
                onValueChange = { onEvent(AccountUiEvent.OnUrlTextFieldValueChange(it)) },
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.url),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp)
            )
            Button(
                onClick = {
                    if (state.urlValue.isNotBlank()) {
                        onEvent(AccountUiEvent.OnUpdateProfilePicture(state.urlValue))
                    }
                    onEvent(AccountUiEvent.OnShowUrlDialog(false))

                },
                modifier = Modifier.padding(bottom = 10.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text(
                    text = stringResource(id = R.string.done),
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
        }
    }

    if (state.showConfirmationDialog) {
        ConfirmationDialog(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            onDismiss = { onEvent(AccountUiEvent.OnShowConfirmationDialog(false)) },
            onConfirm = {
                onEvent(AccountUiEvent.OnShowConfirmationDialog(false))
                onEvent(AccountUiEvent.OnSignOut)
            }
        ) {
            Text(
                text = stringResource(id = R.string.signed_using_anonymous_account_message),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onNavigate: (Screen) -> Unit,
    onBackButtonClick: () -> Unit,
    onUserLoggedOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult

            val fileInputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val size = fileInputStream?.available()
            fileInputStream?.close()

            size.takeIf { it != null }?.let { imageSize ->
                if (imageSize >= 50000) {
                    compressImage(
                        context = context,
                        imageUri = uri,
                        quality = 20
                    )?.let { compressedUri ->
                        viewModel.uiEventHandler(AccountUiEvent.OnUpdateProfilePicture(compressedUri.toString()))
                        viewModel.uiEventHandler(
                            AccountUiEvent.OnSaveUserProfilePicture(
                                compressedUri
                            )
                        )
                    }
                } else {
                    viewModel.uiEventHandler(AccountUiEvent.OnUpdateProfilePicture(uri.toString()))
                    viewModel.uiEventHandler(AccountUiEvent.OnSaveUserProfilePicture(uri))
                }
            } ?: run {
                viewModel.uiEventHandler(AccountUiEvent.OnUpdateProfilePicture(uri.toString()))
                viewModel.uiEventHandler(AccountUiEvent.OnSaveUserProfilePicture(uri))
            }

            viewModel.uiEventHandler(AccountUiEvent.OnShowBottomSheet(false))
        }
    )

    LaunchedEffect(Unit) {
        // get user's data
        viewModel.uiEventHandler(AccountUiEvent.OnGetUserData)
    }

    LaunchedEffect(uiState.userData) {
        if (uiState.userData == null) onUserLoggedOut()
    }

    AccountScreenContent(
        state = uiState,
        bottomSheetState = sheetState,
        onEvent = { event ->
            when (event) {
                AccountUiEvent.OnSinglePicturePickerLaunch -> {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }

                is AccountUiEvent.OnNavigate -> {
                    onNavigate(event.destination)
                }

                AccountUiEvent.OnBackButtonClick -> {
                    onBackButtonClick()
                }

                else -> viewModel.uiEventHandler(event as AccountUiEvent.NonDirect)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AccountScreenPreview() {
    AccountScreenContent(
        state = AccountState(),
        bottomSheetState = SheetState(false, LocalDensity.current, SheetValue.Hidden),
        onEvent = {}
    )
}