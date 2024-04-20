package com.bytecause.lenslex.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
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
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.ui.components.AccountInfoItem
import com.bytecause.lenslex.ui.components.AccountInfoType
import com.bytecause.lenslex.ui.components.ConfirmationDialog
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.ui.components.CredentialsDialog
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.LinkAccountItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.CredentialChangeResult
import com.bytecause.lenslex.ui.screens.viewmodel.Provider
import com.bytecause.lenslex.ui.screens.viewmodel.UserAccountDetails
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
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
    userDetails: UserAccountDetails?,
    credentialValidationResult: CredentialValidationResult?,
    showCredentialUpdateDialog: CredentialType?,
    linkedProviders: List<Provider>?,
    snackBarHostState: SnackbarHostState,
    showConfirmationDialog: Boolean,
    onShowSnackBar: (String) -> Unit,
    onLinkButtonClick: (Provider) -> Unit,
    onDialogCredentialChanged: (Credentials.Sensitive) -> Unit,
    onAccountInfoChange: (CredentialType) -> Unit,
    onEnteredCredential: (Credentials) -> Unit,
    onCredentialsDialogDismiss: (CredentialType) -> Unit,
    onDeleteAccountButtonClick: () -> Unit,
    onDismissConfirmationDialog: () -> Unit,
    onConfirmConfirmationDialog: () -> Unit,
    onNavigateBack: () -> Unit
) {

    val snackBarAnonymousAccountMessage =
        stringResource(id = R.string.email_and_password_cant_be_changed)

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account_settings,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                        isLinked = linkedProviders?.contains(Provider.Google) == true,
                        onLinkButtonClick = { onLinkButtonClick(Provider.Google) }
                    )

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    LinkAccountItem(
                        leadingIconId = R.drawable.baseline_alternate_email_24,
                        contentDescription = R.string.link_email_account,
                        accountProviderName = "Email",
                        isLinked = linkedProviders?.contains(Provider.Email) == true,
                        onLinkButtonClick = { onLinkButtonClick(Provider.Email) }
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
                        userCredential = userDetails?.uid,
                        isChangeable = false,
                        isAnonymous = userDetails?.isAnonymous,
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
                        userCredential = userDetails?.let {
                            SimpleDateFormat.getDateInstance()
                                .format(Date(it.creationTimeStamp ?: 0))
                        },
                        isChangeable = false,
                        isAnonymous = userDetails?.isAnonymous
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
                        userCredential = userDetails?.userName,
                        isChangeable = true,
                        isAnonymous = userDetails?.isAnonymous,
                        onAccountInfoChange = { onAccountInfoChange(CredentialType.Username) }
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
                        userCredential = userDetails?.email,
                        isChangeable = true,
                        isAnonymous = userDetails?.isAnonymous,
                        showSnackBar = { onShowSnackBar(snackBarAnonymousAccountMessage) },
                        onAccountInfoChange = { onAccountInfoChange(CredentialType.Email) }
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
                        isAnonymous = userDetails?.isAnonymous,
                        showSnackBar = { onShowSnackBar(snackBarAnonymousAccountMessage) },
                        onAccountInfoChange = { onAccountInfoChange(CredentialType.Password) }
                    )
                }

                OutlinedButton(
                    onClick = { onDeleteAccountButtonClick() },
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

            if (showConfirmationDialog) {
                ConfirmationDialog(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(),
                    onDismiss = { onDismissConfirmationDialog() },
                    onConfirm = {
                        onConfirmConfirmationDialog()
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

            showCredentialUpdateDialog?.let {
                CredentialsDialog(
                    credentialValidationResult = credentialValidationResult,
                    credentialType = it,
                    onDismiss = { onCredentialsDialogDismiss(it) },
                    onEnteredCredential = { credential -> onEnteredCredential(credential) },
                    onCredentialChanged = { credential -> onDialogCredentialChanged(credential) }
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
    onNavigateBack: () -> Unit,
    onUserLoggedOut: () -> Unit
) {

    val userDetails by viewModel.getUserAccountDetails.collectAsStateWithLifecycle()
    val credentialValidationResult by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()
    val credentialChangeState by viewModel.credentialChangeState.collectAsStateWithLifecycle()
    val showCredentialUpdateDialog by viewModel.showCredentialUpdateDialog.collectAsStateWithLifecycle()
    val linkedProviders by viewModel.getProviders.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    var showConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val googleLinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    viewModel.signInWithGoogleIntent(intent = result.data ?: return@launch)
                }
            }
        }
    )

    val googleReauthorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    if (credentialChangeState is CredentialChangeResult.Failure.ReauthorizationRequired) {
                        val savedFailureState =
                            (credentialChangeState as CredentialChangeResult.Failure.ReauthorizationRequired)
                        viewModel.reauthenticateWithGoogleIntent(
                            intent = result.data ?: return@launch,
                            email = savedFailureState.email,
                            password = savedFailureState.password
                        )
                    }
                }
            }
        }
    )

    LaunchedEffect(key1 = userDetails) {
        if (userDetails == null) onUserLoggedOut()
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
                if (linkedProviders?.contains(Provider.Google) == true) {
                    coroutineScope.launch {
                        val reauthorizationIntentSender = viewModel.signInViaGoogle()
                        googleReauthorizationLauncher.launch(
                            IntentSenderRequest.Builder(
                                reauthorizationIntentSender ?: return@launch
                            ).build()
                        )
                    }.invokeOnCompletion {
                        viewModel.resetCredentialChangeState()
                    }
                } else viewModel.showCredentialUpdateDialog(CredentialType.Reauthorization)
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
        userDetails = userDetails,
        credentialValidationResult = credentialValidationResult,
        showCredentialUpdateDialog = showCredentialUpdateDialog,
        linkedProviders = linkedProviders,
        snackBarHostState = snackBarHostState,
        onShowSnackBar = {
            coroutineScope.launch {
                snackBarHostState.showSnackbar(it)
            }
        },
        showConfirmationDialog = showConfirmationDialog,
        onLinkButtonClick = { provider ->
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
                        coroutineScope.launch {
                            val signInIntentSender = viewModel.signInViaGoogle()
                            googleLinkLauncher.launch(
                                IntentSenderRequest.Builder(
                                    signInIntentSender ?: return@launch
                                ).build()
                            )
                        }
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
                            viewModel.reauthenticateUsingEmailAndPassword(
                                credentials.email,
                                credentials.password
                            )
                        }
                    }

                    is CredentialType.AccountLink -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            viewModel.linkProvider(
                                credential as Credentials.Sensitive,
                                Provider.Email
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
                            viewModel.updateEmail((credential as Credentials.Sensitive.EmailUpdateCredential).email)
                        }
                    }

                    is CredentialType.Password -> {
                        if (credentialValidationResult is CredentialValidationResult.Valid) {
                            viewModel.updatePassword((credential as Credentials.Sensitive.PasswordUpdateCredential).password)
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
        },
        onNavigateBack = { onNavigateBack() }
    )
}

@Composable
@Preview
fun AccountSettingsScreenPreview() {
    AccountSettingsScreenContent(
        userDetails = null,
        credentialValidationResult = null,
        showCredentialUpdateDialog = null,
        linkedProviders = null,
        snackBarHostState = SnackbarHostState(),
        showConfirmationDialog = false,
        onShowSnackBar = {},
        onLinkButtonClick = {},
        onDialogCredentialChanged = {},
        onAccountInfoChange = {},
        onEnteredCredential = {},
        onCredentialsDialogDismiss = {},
        onDeleteAccountButtonClick = {},
        onDismissConfirmationDialog = {},
        onConfirmConfirmationDialog = {},
        onNavigateBack = {}
    )
}