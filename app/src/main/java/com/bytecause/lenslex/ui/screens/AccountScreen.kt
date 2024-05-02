package com.bytecause.lenslex.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.bytecause.lenslex.data.remote.FirebaseCloudStorage
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.AppLanguageRow
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.ProfilePicture
import com.bytecause.lenslex.ui.components.RowItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.util.BlurTransformation
import com.bytecause.lenslex.util.compressImage
import org.koin.androidx.compose.koinViewModel
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreenContent(
    profilePicture: String,
    isEditing: Boolean,
    nameTextFieldValue: String,
    urlTextFieldValue: String,
    isAnonymous: Boolean,
    showConfirmationDialog: Boolean,
    showLanguageDialog: Boolean,
    showBottomSheet: Boolean,
    showUrlDialog: Boolean,
    bottomSheetState: SheetState,
    onUpdateName: () -> Unit,
    onUpdateProfilePicture: () -> Unit,
    onEditChange: () -> Unit,
    onChangeFirebaseLanguage: (String) -> Unit,
    onShowConfirmationDialog: (Boolean) -> Unit,
    onShowLanguageDialog: (Boolean) -> Unit,
    onShowBottomSheet: (Boolean) -> Unit,
    onShowUrlDialog: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onSinglePicturePickerLaunch: () -> Unit,
    onNameTextFieldValueChange: (String) -> Unit,
    onUrlTextFieldValueChange: (String) -> Unit,
    onNavigate: (NavigationItem) -> Unit,
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            ) {
                onBackButtonClick()
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
                                    .data(profilePicture.takeIf { it != "null" }
                                        ?: R.drawable.default_account_image)
                                    .transformations(BlurTransformation(context, 15))
                                    .build()
                            } else profilePicture.takeIf { it != "null" }
                                ?: R.drawable.default_account_image,
                            contentDescription = "avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPaddingValues)
                    .padding(top = 120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Divider(thickness = 1, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEditing) {
                        Text(
                            text = nameTextFieldValue,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        TextField(
                            value = nameTextFieldValue, onValueChange = {
                                onNameTextFieldValueChange(it)
                            },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onUpdateName()
                            onEditChange()
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Filled.Check else Icons.Filled.Create,
                            contentDescription = ""
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(thickness = 2, color = MaterialTheme.colorScheme.primary)

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
                        onShowLanguageDialog(true)
                    }

                    Divider(thickness = 1, color = Color.Gray)

                    RowItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconId = R.drawable.baseline_manage_accounts_24,
                        contentDescription = R.string.account_settings,
                        text = R.string.account_settings
                    ) {
                        onNavigate(NavigationItem.AccountSettings)
                    }

                    Divider(thickness = 1, color = Color.Gray)

                    RowItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIconId = R.drawable.baseline_logout_24,
                        contentDescription = R.string.sign_out,
                        text = R.string.sign_out
                    ) {
                        if (isAnonymous) onShowConfirmationDialog(true)
                        else onSignOut()
                    }
                }
            }

            ProfilePicture(
                profilePicture = profilePicture,
                modifier = Modifier.padding(top = 130.dp)
            ) {
                onShowBottomSheet(true)
            }

            if (showLanguageDialog) {
                Dialog(
                    title = stringResource(id = R.string.choose_language),
                    onDismiss = { onShowLanguageDialog(false) }
                ) {

                    AppLanguageRow(
                        langCode = "eng",
                        isChecked = AppCompatDelegate.getApplicationLocales()[0]?.isO3Language == "eng"
                    ) {
                        onShowLanguageDialog(false)

                        onChangeFirebaseLanguage("en")
                        val appLocale: LocaleListCompat =
                            LocaleListCompat.forLanguageTags("en-EN")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }

                    AppLanguageRow(
                        langCode = "cze",
                        isChecked = AppCompatDelegate.getApplicationLocales()[0]?.isO3Language == "ces"
                    ) {
                        onShowLanguageDialog(false)

                        onChangeFirebaseLanguage("cs")
                        val appLocale: LocaleListCompat =
                            LocaleListCompat.forLanguageTags("cs-CZ")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { onShowBottomSheet(false) },
                sheetState = bottomSheetState,
            ) {

                RowItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingIconId = R.drawable.baseline_image_24,
                    contentDescription = R.string.pick_profile_picture_from_storage,
                    text = R.string.pick_profile_picture_from_storage
                ) {
                    onShowBottomSheet(false)
                    onSinglePicturePickerLaunch()
                }

                RowItem(
                    modifier = Modifier.fillMaxWidth(),
                    leadingIconId = R.drawable.baseline_link_24,
                    contentDescription = R.string.set_profile_picture_from_url,
                    text = R.string.set_profile_picture_from_url
                ) {
                    onShowBottomSheet(false)
                    onShowUrlDialog(true)
                }
            }
        }
    }

    if (showUrlDialog) {
        Dialog(
            title = stringResource(id = R.string.type_picture_url),
            onDismiss = { onShowUrlDialog(false) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = urlTextFieldValue,
                onValueChange = { onUrlTextFieldValueChange(it) },
                supportingText = { Text(text = "URL") },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp)
            )
            Button(
                onClick = {
                    if (urlTextFieldValue.isNotBlank()) {
                        onUpdateProfilePicture()
                    }
                    onShowUrlDialog(false)
                }, modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            onDismiss = { onShowConfirmationDialog(false) },
            onConfirm = {
                onShowConfirmationDialog(false)
                onSignOut()
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
    onNavigate: (NavigationItem) -> Unit,
    onBackButtonClick: () -> Unit,
    onUserLoggedOut: () -> Unit
) {

    val getSignedInUser by viewModel.getSignedInUser.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()

    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var nameTextFieldValue by rememberSaveable {
        mutableStateOf(getSignedInUser?.userName?.takeIf { it.isNotBlank() }
            ?: getSignedInUser?.userId?.takeIf { it.isNotBlank() } ?: "")
    }

    var profilePicture by rememberSaveable {
        mutableStateOf(getSignedInUser?.profilePictureUrl.toString())
    }

    var showUrlDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showLanguageDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var isEditing by rememberSaveable {
        mutableStateOf(false)
    }

    var urlTextFieldValue by rememberSaveable {
        mutableStateOf("")
    }

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
                        profilePicture = compressedUri.toString()
                        FirebaseCloudStorage().saveUserProfilePicture(compressedUri)
                    }
                } else {
                    profilePicture = uri.toString()
                    FirebaseCloudStorage().saveUserProfilePicture(uri)
                }
            } ?: run {
                profilePicture = uri.toString()
                FirebaseCloudStorage().saveUserProfilePicture(uri)
            }

            showBottomSheet = false
        }
    )

    // Refresh user's firebase data
    LaunchedEffect(Unit) {
        viewModel.reload()
    }

    LaunchedEffect(getSignedInUser) {
        if (getSignedInUser == null) onUserLoggedOut()
        else {
            nameTextFieldValue = getSignedInUser?.userName?.takeIf { it.isNotBlank() }
                ?: getSignedInUser?.userId?.takeIf { it.isNotBlank() } ?: ""
            profilePicture = getSignedInUser?.profilePictureUrl.toString()
        }
    }

    AccountScreenContent(
        profilePicture = profilePicture,
        isEditing = isEditing,
        nameTextFieldValue = nameTextFieldValue,
        urlTextFieldValue = urlTextFieldValue,
        isAnonymous = viewModel.isAccountAnonymous,
        showConfirmationDialog = showConfirmationDialog,
        showLanguageDialog = showLanguageDialog,
        showBottomSheet = showBottomSheet,
        showUrlDialog = showUrlDialog,
        bottomSheetState = sheetState,
        onUpdateName = {
            viewModel.updateName(nameTextFieldValue)

        },
        onUpdateProfilePicture = {
            viewModel.updateProfilePicture(Uri.parse(urlTextFieldValue))
            profilePicture = urlTextFieldValue
        },
        onEditChange = { isEditing = !isEditing },
        onChangeFirebaseLanguage = { viewModel.changeFirebaseLanguageCode(it) },
        onShowConfirmationDialog = { showConfirmationDialog = it },
        onShowLanguageDialog = { showLanguageDialog = it },
        onShowBottomSheet = { showBottomSheet = it },
        onShowUrlDialog = { showUrlDialog = it },
        onSignOut = {
            viewModel.signOut()
            onUserLoggedOut()
        },
        onSinglePicturePickerLaunch = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        onNameTextFieldValueChange = { nameTextFieldValue = it },
        onUrlTextFieldValueChange = { urlTextFieldValue = it },
        onNavigate = { onNavigate(it) },
        onBackButtonClick = { onBackButtonClick() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AccountScreenPreview() {
    AccountScreenContent(
        profilePicture = "",
        isEditing = false,
        nameTextFieldValue = "Mikailo",
        urlTextFieldValue = "",
        isAnonymous = false,
        showConfirmationDialog = false,
        showLanguageDialog = false,
        showBottomSheet = false,
        showUrlDialog = false,
        bottomSheetState = SheetState(false, LocalDensity.current, SheetValue.Hidden),
        onUpdateName = {},
        onUpdateProfilePicture = {},
        onEditChange = {},
        onChangeFirebaseLanguage = {},
        onShowConfirmationDialog = {},
        onShowLanguageDialog = {},
        onShowBottomSheet = {},
        onShowUrlDialog = {},
        onSignOut = {},
        onSinglePicturePickerLaunch = {},
        onNameTextFieldValueChange = {},
        onUrlTextFieldValueChange = {},
        onNavigate = {},
        onBackButtonClick = {}
    )
}