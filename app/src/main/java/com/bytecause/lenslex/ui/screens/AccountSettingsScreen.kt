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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.bytecause.lenslex.models.uistate.AccountSettingsState
import com.bytecause.lenslex.ui.components.AccountInfoItem
import com.bytecause.lenslex.ui.components.AccountInfoType
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.ui.components.CredentialsDialog
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.LinkAccountItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.AccountSettingsUiEvent
import com.bytecause.lenslex.ui.interfaces.CredentialChangeResult
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
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
    onEvent: (AccountSettingsUiEvent) -> Unit,
    onShowSnackBar: (String) -> Unit,
    /*onLinkButtonClick: (Provider) -> Unit,
    onDialogCredentialChanged: (Credentials.Sensitive) -> Unit,
    onAccountInfoChange: (CredentialType) -> Unit,
    onEnteredCredential: (Credentials) -> Unit,
    onCredentialsDialogDismiss: (CredentialType) -> Unit,
    onDeleteAccountButtonClick: () -> Unit,
    onDismissConfirmationDialog: () -> Unit,
    onConfirmConfirmationDialog: () -> Unit,*/
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account_settings,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            ) {
                onNavigateBack()
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            if (!isExpandedScreen && LocalOrientationMode.invoke() != OrientationMode.Landscape) {
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

                        Divider(
                            thickness = 1,
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

                        Divider(
                            thickness = 1,
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

                        Divider(
                            thickness = 1,
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

                        Divider(
                            thickness = 1,
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
                            showSnackBar = { onShowSnackBar(context.resources.getString(R.string.email_and_password_cant_be_changed)) },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Email
                                    )
                                )
                            }
                        )

                        Divider(
                            thickness = 1,
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
                            showSnackBar = { onShowSnackBar(context.resources.getString(R.string.email_and_password_cant_be_changed)) },
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
                        .verticalScroll(rememberScrollState())
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

                            Divider(
                                thickness = 1,
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

                        Divider(
                            thickness = 1,
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

                        Divider(
                            thickness = 1,
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

                        Divider(
                            thickness = 1,
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
                            showSnackBar = { onShowSnackBar(context.resources.getString(R.string.email_and_password_cant_be_changed)) },
                            onAccountInfoChange = {
                                onEvent(
                                    AccountSettingsUiEvent.OnShowCredentialDialog(
                                        CredentialType.Email
                                    )
                                )
                            }
                        )

                        Divider(
                            thickness = 1,
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
                            showSnackBar = { onShowSnackBar(context.resources.getString(R.string.email_and_password_cant_be_changed)) },
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

            if (state.showConfirmationDialog) {
                ConfirmationDialog(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(),
                    onDismiss = { onEvent(AccountSettingsUiEvent.OnDismissConfirmationDialog) },
                    onConfirm = {
                        onEvent(AccountSettingsUiEvent.OnConfirmConfirmationDialog)
                    }
                ) {
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
            }

            state.showCredentialUpdateDialog?.let {
                CredentialsDialog(
                    credentialValidationResult = state.credentialValidationResult,
                    credentialType = it,
                    onDismiss = { onEvent(AccountSettingsUiEvent.OnCredentialsDialogDismiss(it)) },
                    onEnteredCredential = { credential ->
                        onEvent(
                            AccountSettingsUiEvent.OnEnteredCredential(
                                credential
                            )
                        )
                    },
                    onCredentialChanged = { credential ->
                        onEvent(
                            AccountSettingsUiEvent.OnDialogCredentialChanged(
                                credential
                            )
                        )
                    }
                )
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
    val credentialChangeState by viewModel.credentialChangeState.collectAsStateWithLifecycle()

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

    LaunchedEffect(credentialChangeState) {
        when (credentialChangeState) {
            is CredentialChangeResult.Success -> {
                coroutineScope.launch {
                    val messageId =
                        (credentialChangeState as CredentialChangeResult.Success).message
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

                            viewModel.reauthenticateWithGoogle(
                                authCredential,
                                credentialChangeState as CredentialChangeResult.Failure.ReauthorizationRequired
                            )
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
                    (credentialChangeState as CredentialChangeResult.Failure.Error).exception

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
        onShowSnackBar = {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(it)
            }
        },
        onEvent = {
            viewModel.uiEventHandler(it)
        },
        /*onLinkButtonClick = { provider ->
            when (provider) {
                is Provider.Email -> {
                    if (linkedProviders?.contains(Provider.Email) == true) {
                        viewModel.unlinkProvider(Provider.Email)
                    } else viewModel.showCredentialUpdateDialog(CredentialType.AccountLink)
                }

                is Provider.Google -> {
                    if (linkedProviders?.contains(Provider.Google) == true) {
                        viewModel.unlinkProvider(Provider.Google)
                    } else {
                        viewModel.linkGoogleProvider(context)
                    }
                }
            }
        },
        onDialogCredentialChanged = { credential ->
            viewModel.saveCredentialValidationResult(
                ValidationUtil.areCredentialsValid(
                    credential
                )
            )
        },
        onAccountInfoChange = {
            when (it) {
                is CredentialType.Username -> {
                    viewModel.showCredentialUpdateDialog(CredentialType.Username)
                }

                is CredentialType.Email -> {
                    viewModel.showCredentialUpdateDialog(CredentialType.Email)
                }

                is CredentialType.Password -> {
                    viewModel.showCredentialUpdateDialog(CredentialType.Password)
                }

                else -> return@AccountSettingsScreenContent
            }
        },
        onEnteredCredential = { credential ->
            showCredentialUpdateDialog?.let {
                when (it) {
                    is CredentialType.Reauthorization -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            val credentials = credential as Credentials.Sensitive.SignInCredentials
                            viewModel.reauthenticateUsingEmailAndPassword(credentials)
                        }
                    }

                    is CredentialType.AccountLink -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            viewModel.linkEmailProvider(
                                credential as Credentials.Sensitive
                            )
                        }
                    }

                    is CredentialType.Username -> {
                        (credential as Credentials.Insensitive.UsernameUpdate)
                            .takeIf { user -> user.username.isNotBlank() }
                            ?.let { user ->
                                viewModel.updateUserName(user.username)
                            }
                        viewModel.showCredentialUpdateDialog(null)
                    }

                    is CredentialType.Email -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            viewModel.updateEmail((credential as Credentials.Sensitive.EmailCredential).email)
                        }
                    }

                    is CredentialType.Password -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            viewModel.updatePassword((credential as Credentials.Sensitive.PasswordCredential).password)
                        }
                    }
                }

                if (credentialValidationResult is CredentialValidationResult.Valid) {
                    viewModel.showCredentialUpdateDialog(null)
                }
            }
        },
        onCredentialsDialogDismiss = {
            if (it is CredentialType.Reauthorization) viewModel.resetCredentialChangeState()
            viewModel.showCredentialUpdateDialog(null)
        },
        onDeleteAccountButtonClick = {
            showConfirmationDialog = true
        },
        onDismissConfirmationDialog = {
            showConfirmationDialog = false
        },
        onConfirmConfirmationDialog = {
            showConfirmationDialog = false
            viewModel.deleteAccount()
        },*/
        onNavigateBack = { onNavigateBack() }
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
        onShowSnackBar = {},
        onEvent = {},
        onNavigateBack = {}
    )
}