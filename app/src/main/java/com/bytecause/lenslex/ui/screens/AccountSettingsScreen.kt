package com.bytecause.lenslex.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.ui.components.AccountInfoItem
import com.bytecause.lenslex.ui.components.AccountInfoType
import com.bytecause.lenslex.ui.components.CredentialType
import com.bytecause.lenslex.ui.components.CredentialsDialog
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.LinkAccountItem
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.CredentialChangeResult
import com.bytecause.lenslex.ui.screens.viewmodel.Provider
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {

    val userDetails by viewModel.getUserAccountDetails.collectAsStateWithLifecycle()
    val credentialValidationResult by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()
    val credentialChangeState by viewModel.credentialChangeState.collectAsStateWithLifecycle()
    val showCredentialUpdateDialog by viewModel.showCredentialUpdateDialog.collectAsStateWithLifecycle()
    val linkedProviders by viewModel.getProviders.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarAnonymousAccountMessage =
        stringResource(id = R.string.email_and_password_cant_be_changed)
    val snackBarSuccessMessage = stringResource(id = R.string.success)

    val coroutineScope = rememberCoroutineScope()

    val googleLinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                coroutineScope.launch {
                    viewModel.signInWithGoogleIntent(intent = result.data ?: return@launch)
                }
            }
        }
    )

    val googleReauthorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                coroutineScope.launch {
                    viewModel.reauthorizeWithGoogle(intent = result.data ?: return@launch)
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.account_settings,
                navigationIcon = Icons.Filled.ArrowBack,
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
                        leadingIcon = R.drawable.google_logo,
                        contentDescription = R.string.link_google_account,
                        accountProvider = "Google",
                        isLinked = linkedProviders?.contains(Provider.Google) == true,
                        onLinkButtonClick = {
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
                    )

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    LinkAccountItem(
                        leadingIcon = R.drawable.baseline_alternate_email_24,
                        contentDescription = R.string.link_email_account,
                        accountProvider = "Email",
                        isLinked = linkedProviders?.contains(Provider.Email) == true,
                        onLinkButtonClick = {
                            if (linkedProviders?.contains(Provider.Email) == true) {
                                viewModel.unlinkProvider(Provider.Email)
                            } else {
                                //viewModel.linkProvider(Provider.Firebase)
                                viewModel.showCredentialUpdateDialog(CredentialType.AccountLink)
                            }
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
                        leadingIcon = R.drawable.id,
                        contentDescription = R.string.user_id,
                        accountInfoType = AccountInfoType.Uid,
                        userCredential = userDetails?.uid,
                        isChangeable = false,
                        isAnonymous = userDetails?.isAnonymous,
                    ) { }

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    AccountInfoItem(
                        leadingIcon = R.drawable.calendar,
                        contentDescription = R.string.account_creation_date,
                        accountInfoType = AccountInfoType.CreationDate,
                        userCredential = SimpleDateFormat.getDateInstance()
                            .format(Date(userDetails?.creationTimeStamp!!)),
                        isChangeable = false,
                        isAnonymous = userDetails?.isAnonymous

                    ) { }

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    AccountInfoItem(
                        leadingIcon = R.drawable.baseline_perm_identity_24,
                        contentDescription = R.string.username,
                        accountInfoType = AccountInfoType.UserName,
                        userCredential = userDetails?.userName,
                        isChangeable = true,
                        isAnonymous = userDetails?.isAnonymous
                    ) {
                        viewModel.showCredentialUpdateDialog(CredentialType.Username)
                    }

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    AccountInfoItem(
                        leadingIcon = R.drawable.baseline_alternate_email_24,
                        contentDescription = R.string.account_email,
                        accountInfoType = AccountInfoType.Email,
                        userCredential = userDetails?.email,
                        isChangeable = true,
                        isAnonymous = userDetails?.isAnonymous,
                        showSnackBar = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(snackBarAnonymousAccountMessage)
                            }
                        }
                    ) {
                        viewModel.showCredentialUpdateDialog(CredentialType.Email)
                    }

                    Divider(
                        thickness = 1,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )

                    AccountInfoItem(
                        leadingIcon = R.drawable.baseline_password_24,
                        contentDescription = R.string.account_password,
                        accountInfoType = AccountInfoType.Password,
                        userCredential = "********",
                        isChangeable = true,
                        isAnonymous = userDetails?.isAnonymous,
                        showSnackBar = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(snackBarAnonymousAccountMessage)
                            }
                        }
                    ) {
                        viewModel.showCredentialUpdateDialog(CredentialType.Password)
                    }
                }
            }

            showCredentialUpdateDialog?.let {
                CredentialsDialog(
                    credentialValidationResult = credentialValidationResult,
                    credentialType = it,
                    onDismiss = {
                        if (it is CredentialType.Reauthorization) viewModel.resetCredentialChangeState()
                        viewModel.showCredentialUpdateDialog(null)
                    },
                    onEnteredCredential = { credential ->
                        when (it) {
                            is CredentialType.Reauthorization -> {
                                if (credentialValidationResult is CredentialValidationResult.Valid) {
                                    val credentials = credential as Credentials.SignInCredentials

                                    viewModel.showCredentialUpdateDialog(null)
                                    viewModel.reauthenticateUsingEmailAndPassword(
                                        credentials.email,
                                        credentials.password
                                    )
                                }
                            }

                            is CredentialType.AccountLink -> {
                                if (credentialValidationResult is CredentialValidationResult.Valid) {
                                    viewModel.linkProvider(credential, Provider.Email)
                                    viewModel.showCredentialUpdateDialog(null)
                                }
                            }

                            is CredentialType.Username -> {
                                //viewModel.updateUserName((credential as Credentials.EmailUpdateCredential).email)
                                viewModel.showCredentialUpdateDialog(null)
                            }

                            is CredentialType.Email -> {
                                if (credentialValidationResult is CredentialValidationResult.Valid) {
                                    Log.d("idk", "Valid")
                                    viewModel.updateEmail((credential as Credentials.EmailUpdateCredential).email)
                                    viewModel.showCredentialUpdateDialog(null)
                                }
                            }

                            is CredentialType.Password -> {
                                if (credentialValidationResult is CredentialValidationResult.Valid) {
                                    Log.d("idk", "Valid")
                                    viewModel.updatePassword((credential as Credentials.PasswordUpdateCredential).password)
                                    viewModel.showCredentialUpdateDialog(null)
                                }
                            }
                        }
                    },
                    onCredentialChanged = { credential ->
                        viewModel.saveCredentialValidationResult(
                            ValidationUtil.areCredentialsValid(
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

            LaunchedEffect(
                key1 = credentialChangeState != null
            ) {
                when (credentialChangeState) {
                    is CredentialChangeResult.Success -> {
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar((credentialChangeState as CredentialChangeResult.Success).message)
                        }
                    }

                    is CredentialChangeResult.Failure.Error -> {

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
                            }
                        } else viewModel.showCredentialUpdateDialog(CredentialType.Reauthorization)
                    }

                    else -> {

                    }
                }
            }
        }
    }
}