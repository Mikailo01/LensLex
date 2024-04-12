package com.bytecause.lenslex.ui.screens

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.FirebaseCloudStorage
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.ProfilePicture
import com.bytecause.lenslex.ui.components.RowItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.util.BlurTransformation
import com.bytecause.lenslex.util.compressImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel = koinViewModel(),
    onNavigate: (NavigationItem) -> Unit,
    onBackButtonClick: () -> Unit
) {
    val getSignedInUser by viewModel.getSignedInUser.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var showBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var nameTextFieldValue by mutableStateOf(getSignedInUser?.userName?.takeIf { it.isNotBlank() }
        ?: getSignedInUser?.userId?.takeIf { it.isNotBlank() }
        ?: "")

    var profilePicture by mutableStateOf(getSignedInUser?.profilePictureUrl.toString())

    var showUrlDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var isEditing by rememberSaveable {
        mutableStateOf(false)
    }

    var urlTextFieldValue by rememberSaveable {
        mutableStateOf("")
    }

    val context = LocalContext.current
    val packageManager: PackageManager = context.packageManager
    val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
    val componentName: ComponentName = intent.component!!
    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)

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

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account,
                navigationIcon = Icons.Filled.ArrowBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                onBackButtonClick()
            }
        }
    ) { innerPaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .blur(3.dp) // Ignored if Android API level < 31
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

                    Divider(thickness = 2, color = Color.Gray)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPaddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ProfilePicture(
                    profilePicture = profilePicture,
                    cornerIcon = R.drawable.baseline_camera_alt_24,
                    modifier = Modifier.padding(top = 80.dp)
                ) {
                    showBottomSheet = true
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                nameTextFieldValue = it
                            },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.updateName(nameTextFieldValue)
                            isEditing = !isEditing
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

                RowItem(
                    leadingIconId = R.drawable.baseline_language_24,
                    contentDescription = R.string.select_language,
                    text = R.string.language
                ) {

                }

                Divider(thickness = 1, color = Color.Gray)

                RowItem(
                    leadingIconId = R.drawable.baseline_manage_accounts_24,
                    contentDescription = R.string.account_settings,
                    text = R.string.account_settings
                ) {
                    onNavigate(NavigationItem.AccountSettings)
                }

                Divider(thickness = 1, color = Color.Gray)

                RowItem(
                    leadingIconId = R.drawable.baseline_logout_24,
                    contentDescription = R.string.sign_out,
                    text = R.string.sign_out
                ) {
                    if (viewModel.isAccountAnonymous) showConfirmationDialog = true
                    else {
                        coroutineScope.launch {
                            if (viewModel.signOut()) {
                                context.startActivity(restartIntent)
                                Runtime
                                    .getRuntime()
                                    .exit(0)
                            }
                        }
                    }
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                ) {

                    RowItem(
                        leadingIconId = R.drawable.baseline_image_24,
                        contentDescription = R.string.pick_profile_picture_from_storage,
                        text = R.string.pick_profile_picture_from_storage
                    ) {
                        showBottomSheet = false
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }

                    RowItem(
                        leadingIconId = R.drawable.baseline_link_24,
                        contentDescription = R.string.set_profile_picture_from_url,
                        text = R.string.set_profile_picture_from_url
                    ) {
                        showBottomSheet = false
                        showUrlDialog = true
                    }
                }
            }
        }

        if (showUrlDialog) {
            Dialog(
                title = "Type picture URL",
                onDismiss = { showUrlDialog = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = urlTextFieldValue,
                    onValueChange = { urlTextFieldValue = it },
                    supportingText = { Text(text = "URL") },
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp)
                )
                Button(
                    onClick = {
                        if (urlTextFieldValue.isNotBlank()) {
                            viewModel.updateProfilePicture(Uri.parse(urlTextFieldValue))
                            profilePicture = urlTextFieldValue
                        }
                        showUrlDialog = false
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
                onDismiss = { showConfirmationDialog = false }
            ) {
                showConfirmationDialog = false

                coroutineScope.launch {
                    if (viewModel.signOut()) {
                        context.startActivity(restartIntent)
                        Runtime
                            .getRuntime()
                            .exit(0)
                    }
                }
            }
        }
    }
}