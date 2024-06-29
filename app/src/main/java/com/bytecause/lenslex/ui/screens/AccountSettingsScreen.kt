package com.bytecause.lenslex.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.ui.components.AccountInfoItem
import com.bytecause.lenslex.ui.components.AccountInfoType
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.EmailField
import com.bytecause.lenslex.ui.components.LinkAccountItem
import com.bytecause.lenslex.ui.components.PasswordField
import com.bytecause.lenslex.ui.components.PasswordFields
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.AccountSettingsUiEvent
import com.bytecause.lenslex.ui.interfaces.CredentialChangeResult
import com.bytecause.lenslex.ui.interfaces.CredentialType
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.ui.screens.uistate.AccountSettingsConfirmationDialog
import com.bytecause.lenslex.ui.screens.uistate.AccountSettingsState
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreenContent(
    isExpandedScreen: Boolean,
    state: AccountSettingsState,
    snackBarHostState: SnackbarHostState,
    onEvent: (AccountSettingsUiEvent) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account_settings,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) {
                onEvent(AccountSettingsUiEvent.OnNavigateBack)
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!isExpandedScreen && LocalOrientationMode() != OrientationMode.Landscape) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = stringResource(id = R.string.link_account),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                        )
                        LinkAccountItem(
                            leadingIconId = R.drawable.google_logo,
                            contentDescription = R.string.link_google_account,
                            accountProviderName = "Google",
                            isLinked = state.linkedProviders.contains(Provider.Google),
                            onLinkButtonClick = {
                                onEvent(
                                    AccountSettingsUiEvent.OnLinkButtonClick(
                                        Provider.Google
                                    )
                                )
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        LinkAccountItem(
                            leadingIconId = R.drawable.baseline_alternate_email_24,
                            contentDescription = R.string.link_email_account,
                            accountProviderName = stringResource(id = R.string.email),
                            isLinked = state.linkedProviders.contains(Provider.Email),
                            onLinkButtonClick = {
                                onEvent(
                                    AccountSettingsUiEvent.OnLinkButtonClick(
                                        Provider.Email
                                    )
                                )
                            }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = stringResource(id = R.string.account_info),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.id,
                            contentDescriptionId = R.string.user_id,
                            accountInfoType = AccountInfoType.Uid,
                            userCredential = state.userDetails?.uid,
                            isChangeable = false,
                            isAnonymous = state.userDetails?.isAnonymous,
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.calendar,
                            contentDescriptionId = R.string.account_creation_date,
                            accountInfoType = AccountInfoType.CreationDate,
                            userCredential = state.userDetails?.let {
                                SimpleDateFormat.getDateInstance()
                                    .format(Date(it.creationTimeStamp ?: 0))
                            },
                            isChangeable = false,
                            isAnonymous = state.userDetails?.isAnonymous
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_perm_identity_24,
                            contentDescriptionId = R.string.username,
                            accountInfoType = AccountInfoType.UserName,
                            userCredential = state.userDetails?.userName,
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Username
                                    )
                                )
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_alternate_email_24,
                            contentDescriptionId = R.string.account_email,
                            accountInfoType = AccountInfoType.Email,
                            userCredential = state.userDetails?.email,
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            showSnackBar = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowSnackBar(
                                        context.resources.getString(
                                            R.string.email_and_password_cant_be_changed
                                        )
                                    )
                                )
                            },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Email
                                    )
                                )
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_password_24,
                            contentDescriptionId = R.string.account_password,
                            accountInfoType = AccountInfoType.Password,
                            userCredential = "********",
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            showSnackBar = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowSnackBar(
                                        context.resources.getString(
                                            R.string.email_and_password_cant_be_changed
                                        )
                                    )
                                )
                            },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Password
                                    )
                                )
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { onEvent(AccountSettingsUiEvent.OnDeleteAccountButtonClick) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.delete_user),
                                contentDescription = stringResource(id = R.string.delete_account),
                            )
                            Text(
                                text = stringResource(id = R.string.delete_account),
                                modifier = Modifier.padding(end = 10.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .padding(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = stringResource(id = R.string.link_account),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                            )
                            LinkAccountItem(
                                leadingIconId = R.drawable.google_logo,
                                contentDescription = R.string.link_google_account,
                                accountProviderName = "Google",
                                isLinked = state.linkedProviders.contains(Provider.Google),
                                onLinkButtonClick = {
                                    onEvent(
                                        AccountSettingsUiEvent.OnLinkButtonClick(
                                            Provider.Google
                                        )
                                    )
                                }
                            )

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                            )

                            LinkAccountItem(
                                leadingIconId = R.drawable.baseline_alternate_email_24,
                                contentDescription = R.string.link_email_account,
                                accountProviderName = stringResource(id = R.string.email),
                                isLinked = state.linkedProviders.contains(Provider.Email),
                                onLinkButtonClick = {
                                    onEvent(
                                        AccountSettingsUiEvent.OnLinkButtonClick(
                                            Provider.Email
                                        )
                                    )
                                }
                            )
                        }
                        OutlinedButton(
                            onClick = { onEvent(AccountSettingsUiEvent.OnDeleteAccountButtonClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.delete_user),
                                    contentDescription = stringResource(id = R.string.delete_account),
                                )
                                Text(
                                    text = stringResource(id = R.string.delete_account),
                                    modifier = Modifier.padding(end = 10.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = stringResource(id = R.string.account_info),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.id,
                            contentDescriptionId = R.string.user_id,
                            accountInfoType = AccountInfoType.Uid,
                            userCredential = state.userDetails?.uid,
                            isChangeable = false,
                            isAnonymous = state.userDetails?.isAnonymous,
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.calendar,
                            contentDescriptionId = R.string.account_creation_date,
                            accountInfoType = AccountInfoType.CreationDate,
                            userCredential = state.userDetails?.let {
                                SimpleDateFormat.getDateInstance()
                                    .format(Date(it.creationTimeStamp ?: 0))
                            },
                            isChangeable = false,
                            isAnonymous = state.userDetails?.isAnonymous
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_perm_identity_24,
                            contentDescriptionId = R.string.username,
                            accountInfoType = AccountInfoType.UserName,
                            userCredential = state.userDetails?.userName,
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Username
                                    )
                                )
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_alternate_email_24,
                            contentDescriptionId = R.string.account_email,
                            accountInfoType = AccountInfoType.Email,
                            userCredential = state.userDetails?.email,
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            showSnackBar = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowSnackBar(
                                        context.resources.getString(
                                            R.string.email_and_password_cant_be_changed
                                        )
                                    )
                                )
                            },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Email
                                    )
                                )
                            }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                        )

                        AccountInfoItem(
                            leadingIconId = R.drawable.baseline_password_24,
                            contentDescriptionId = R.string.account_password,
                            accountInfoType = AccountInfoType.Password,
                            userCredential = "********",
                            isChangeable = true,
                            isAnonymous = state.userDetails?.isAnonymous,
                            showSnackBar = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowSnackBar(
                                        context.resources.getString(
                                            R.string.email_and_password_cant_be_changed
                                        )
                                    )
                                )
                            },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Password
                                    )
                                )
                            }
                        )
                    }
                }
            }

            state.showCredentialUpdateDialog?.let {
                CredentialsDialog(
                    credentialValidationResult = state.credentialValidationResult,
                    credentialType = it,
                    onEvent = { event -> onEvent(event) }
                )
            }

            when (state.showConfirmationDialog) {
                null -> {
                    // do nothing
                }

                else -> {
                    ConfirmationDialog(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(),
                        title = if (state.showConfirmationDialog is AccountSettingsConfirmationDialog.PasswordChangeWarning) R.string.google_account_will_be_unlinked else -1,
                        onDismiss = { onEvent(AccountSettingsUiEvent.OnDismissConfirmationDialog) },
                        onConfirm = {
                            onEvent(AccountSettingsUiEvent.OnConfirmConfirmationDialog)
                        }
                    ) {
                        when (state.showConfirmationDialog) {
                            AccountSettingsConfirmationDialog.DeleteAccountWarning -> {
                                Row {
                                    Image(
                                        painter = painterResource(id = R.drawable.delete_user),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 10.dp)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.account_deletion_warning_message),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            AccountSettingsConfirmationDialog.PasswordChangeWarning -> {
                                Text(
                                    text = stringResource(id = R.string.google_account_will_be_unlinked_message),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { snackBarData ->
                Snackbar(
                    snackbarData = snackBarData,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel(),
    isExpandedScreen: Boolean,
    onNavigateBack: () -> Unit,
    onUserLoggedOut: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launchGoogleIntent by viewModel.launchGoogleIntent.collectAsStateWithLifecycle()

    var isGoogleIntentLaunched by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.userDetails) {
        if (uiState.userDetails == null) onUserLoggedOut()
    }

    LaunchedEffect(key1 = launchGoogleIntent) {
        if (!isGoogleIntentLaunched) {
            if (launchGoogleIntent) {
                try {
                    forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                    isGoogleIntentLaunched = true
                    val authClient = FirebaseAuthClient()
                    val authCredential = authClient.getGoogleCredential(context)

                    viewModel.linkGoogleProvider(authCredential)
                    isGoogleIntentLaunched = false
                } catch (e: Exception) {
                    isGoogleIntentLaunched = false
                    viewModel.shouldLaunchGoogleIntent(false)
                }
                forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            }
        } else isGoogleIntentLaunched = true
    }

    LaunchedEffect(uiState.credentialChangeResult) {
        when (uiState.credentialChangeResult) {
            is CredentialChangeResult.Success -> {
                coroutineScope.launch {
                    val messageId =
                        (uiState.credentialChangeResult as CredentialChangeResult.Success).message
                    snackBarHostState.showSnackbar(
                        context.getString(messageId)
                    )
                }.invokeOnCompletion {
                    viewModel.resetCredentialChangeState()
                }
            }

            is CredentialChangeResult.Failure.ReauthorizationRequired -> {
                // if user's account is linked with Google, use Credential Manager for faster and
                // simpler reauthentication
                if (uiState.linkedProviders.contains(Provider.Google)) {

                    if (!isGoogleIntentLaunched) {
                        try {
                            forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LOCKED)
                            isGoogleIntentLaunched = true
                            val authClient = FirebaseAuthClient()
                            val authCredential = authClient.getGoogleCredential(context)

                            viewModel.reauthenticateWithGoogle(authCredential)

                            isGoogleIntentLaunched = false
                        } catch (e: Exception) {
                            isGoogleIntentLaunched = false
                            viewModel.resetCredentialChangeState()
                        }
                        forceOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    }

                } else {
                    // Shows dialog with email and password inputs for reauthorization
                    viewModel.uiEventHandler(
                        AccountSettingsUiEvent.OnShowCredentialDialog(
                            CredentialType.Reauthorization
                        )
                    )
                }
            }

            is CredentialChangeResult.Failure.Error -> {
                val castedError =
                    (uiState.credentialChangeResult as CredentialChangeResult.Failure.Error).exception

                when (castedError) {
                    is FirebaseAuthInvalidUserException -> {
                        castedError.message?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }.invokeOnCompletion {
                                viewModel.resetCredentialChangeState()
                            }
                        }
                    }

                    is FirebaseAuthInvalidCredentialsException -> {
                        castedError.message?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }.invokeOnCompletion {
                                viewModel.resetCredentialChangeState()
                            }
                        }
                    }

                    is FirebaseAuthUserCollisionException -> {
                        castedError.message?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }.invokeOnCompletion {
                                viewModel.resetCredentialChangeState()
                            }
                        }
                    }

                    is FirebaseAuthException -> {
                        castedError.message?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }.invokeOnCompletion {
                                viewModel.resetCredentialChangeState()
                            }
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    AccountSettingsScreenContent(
        isExpandedScreen = isExpandedScreen,
        state = uiState,
        snackBarHostState = snackBarHostState,
        onEvent = { event ->
            when (event) {
                is AccountSettingsUiEvent.OnShowSnackBar -> {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(event.message)
                    }
                }

                AccountSettingsUiEvent.OnNavigateBack -> onNavigateBack()

                else -> {
                    viewModel.uiEventHandler(event as AccountSettingsUiEvent.NonDirect)
                }
            }
        }
    )
}

// When Credential Manager is displayed, the device orientation is locked to prevent the coroutine
// from being cancelled when the orientation is changed.
// I couldn't think of a better way to solve this problem, because viewModel
// should not hold an instance of the activity context.
fun forceOrientation(context: Context, activityInfo: Int) {
    (context as? Activity)?.requestedOrientation = activityInfo
}

@Composable
@Preview
fun AccountSettingsScreenPreview() {
    AccountSettingsScreenContent(
        isExpandedScreen = false,
        state = AccountSettingsState(),
        snackBarHostState = SnackbarHostState(),
        onEvent = {}
    )
}

@Composable
fun CredentialsDialog(
    credentialValidationResult: CredentialValidationResult?,
    modifier: Modifier = Modifier,
    credentialType: CredentialType,
    onEvent: (AccountSettingsUiEvent) -> Unit
) {

    var username by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var isEmailError by rememberSaveable {
        mutableStateOf(false)
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPassword by rememberSaveable {
        mutableStateOf("")
    }

    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isPasswordError by rememberSaveable {
        mutableStateOf<List<PasswordErrorType>>(emptyList())
    }

    LaunchedEffect(key1 = credentialValidationResult) {
        isEmailError = when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                credentialValidationResult.isEmailValid != true
            }

            else -> false
        }
    }

    LaunchedEffect(
        key1 = password,
        key2 = confirmPassword
    ) {
        when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                isPasswordError =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            passwordError.cause
                        }

                        else -> emptyList()
                    }
            }

            else -> {
                isPasswordError = emptyList()
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = {
        onEvent(
            AccountSettingsUiEvent.OnCredentialsDialogDismiss(
                credentialType
            )
        )
    }) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {


                when (credentialType) {
                    is CredentialType.Reauthorization -> {
                        Text(text = stringResource(id = R.string.reauthorization))

                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.SignInCredentials(
                                            email,
                                            password
                                        )
                                    )
                                )
                            }
                        )

                        PasswordField(
                            password = password,
                            passwordErrors = isPasswordError,
                            isPasswordEnabled = !isEmailError,
                            isPasswordVisible = isPasswordVisible,
                            onPasswordVisibilityClick = { isPasswordVisible = it },
                            onPasswordValueChange = {
                                password = it
                            },
                            onCredentialChanged = {
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.SignInCredentials(
                                            email,
                                            password
                                        )
                                    )
                                )
                            }
                        )

                    }

                    is CredentialType.AccountLink -> {
                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.EmailCredential(
                                            it
                                        )
                                    )
                                )
                            })

                        PasswordFields(
                            password = password,
                            confirmPassword = confirmPassword,
                            isPasswordEnabled = !(email.isBlank() || isEmailError),
                            isPasswordVisible = isPasswordVisible,
                            passwordErrors = isPasswordError,
                            onPasswordValueChange = { password = it },
                            onConfirmPasswordValueChange = { confirmPassword = it },
                            onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                            onCredentialChanged = {
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.PasswordCredential(
                                            password, confirmPassword
                                        )
                                    )
                                )
                            }
                        )
                    }

                    is CredentialType.Username -> {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_perm_identity_24),
                                    contentDescription = "Enter new username"
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = R.string.username)
                                )
                            })
                    }

                    is CredentialType.Email -> {
                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.EmailCredential(
                                            it
                                        )
                                    )
                                )
                            }
                        )
                    }

                    is CredentialType.Password -> {
                        PasswordFields(
                            password = password,
                            confirmPassword = confirmPassword,
                            isPasswordEnabled = true,
                            isPasswordVisible = isPasswordVisible,
                            passwordErrors = isPasswordError,
                            onPasswordValueChange = { password = it },
                            onConfirmPasswordValueChange = { confirmPassword = it },
                            onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                            onCredentialChanged = {
                                onEvent(
                                    AccountSettingsUiEvent.OnDialogCredentialChanged(
                                        Credentials.Sensitive.PasswordCredential(
                                            password, confirmPassword
                                        )
                                    )
                                )
                            }
                        )
                    }
                }

                OutlinedButton(onClick = {
                    onEvent(
                        AccountSettingsUiEvent.OnEnteredCredential(
                            when (credentialType) {
                                is CredentialType.Reauthorization -> {
                                    Credentials.Sensitive.SignInCredentials(email, password)
                                }

                                is CredentialType.AccountLink -> {
                                    Credentials.Sensitive.SignInCredentials(email, password)
                                }

                                is CredentialType.Username -> {
                                    Credentials.Insensitive.UsernameUpdate(username)
                                }

                                is CredentialType.Email -> {
                                    if (email.isBlank()) {
                                        isEmailError = true
                                        return@OutlinedButton
                                    }
                                    Credentials.Sensitive.EmailCredential(email)
                                }

                                is CredentialType.Password -> {
                                    if (password.isBlank()) {
                                        isPasswordError = listOf(PasswordErrorType.PASSWORD_EMPTY)
                                        return@OutlinedButton
                                    }
                                    Credentials.Sensitive.PasswordCredential(
                                        password,
                                        confirmPassword
                                    )
                                }
                            }
                        )
                    )
                }) {
                    Text(text = stringResource(id = R.string.done))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CredentialsEmailDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Email,
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
fun CredentialsPasswordDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Password,
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
fun CredentialsUsernameDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Username,
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
fun CredentialsReauthorizationDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Reauthorization,
        onEvent = {}
    )
}

@Composable
@Preview(showBackground = true)
fun CredentialsAccountLinkDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.AccountLink,
        onEvent = {}
    )
}