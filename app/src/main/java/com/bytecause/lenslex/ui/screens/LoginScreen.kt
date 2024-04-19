package com.bytecause.lenslex.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.ImageResource
import com.bytecause.lenslex.ui.components.LoginOptionRow
import com.bytecause.lenslex.ui.components.StatefulSignInComp
import com.bytecause.lenslex.ui.components.StatefulSignUpComp
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.ui.theme.blue
import com.bytecause.lenslex.ui.theme.purple
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreenContent(
    signIn: Boolean,
    isLoading: Boolean,
    credentialValidationResultState: CredentialValidationResult?,
    snackBarHostState: SnackbarHostState,
    onCredentialChanged: (Credentials.Sensitive) -> Unit,
    onSignInUsingGoogle: () -> Unit,
    onSignInAnonymously: () -> Unit,
    onCredentialsEntered: (Credentials.Sensitive) -> Unit,
    onSignInAnnotatedStringClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val gradientBackground = Brush.verticalGradient(
        0.2f to purple,
        1.0f to blue,
        startY = 0.0f,
        endY = 1500f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
        ) {
            if (signIn) StatefulSignInComp(
                modifier = Modifier.padding(15.dp),
                credentialValidationResult = credentialValidationResultState,
                isLoading = isLoading,
                onCredentialsEntered = { credentials ->

                    keyboardController?.hide()

                    onCredentialsEntered(
                        Credentials.Sensitive.SignInCredentials(
                            email = credentials.email,
                            password = credentials.password
                        )
                    )
                },
                onCredentialChanged = { credential ->
                    onCredentialChanged(credential)
                },
                onSignInAnnotatedStringClick = { onSignInAnnotatedStringClick() }
            )
            else StatefulSignUpComp(
                modifier = Modifier.padding(15.dp),
                credentialValidationResult = credentialValidationResultState,
                isLoading = isLoading,
                onSignUpButtonClicked = { signUpCredentials ->

                    keyboardController?.hide()
                    onCredentialsEntered(signUpCredentials)
                },
                onCredentialChanged = { credentials ->
                    onCredentialChanged(credentials)
                },
                onSignInAnnotatedStringClick = { onSignInAnnotatedStringClick() }
            )

            Divider(thickness = 2, color = Color.Gray)

            LoginOptionRow(
                modifier = Modifier.padding(top = 10.dp),
                optionImage = ImageResource.Painter(painterResource(id = R.drawable.google_logo)),
                text = stringResource(id = R.string.continue_with_google),
                contentDescription = ""
            ) {

                onSignInUsingGoogle()
            }
            LoginOptionRow(
                optionImage = ImageResource.ImageVector(Icons.Filled.Person),
                text = stringResource(id = R.string.continue_anonymously),
                contentDescription = ""
            ) {
                onSignInAnonymously()
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

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onUserLoggedIn: () -> Unit
) {
    val signUiState by viewModel.signUiState.collectAsStateWithLifecycle()
    val credentialValidationResultState by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()

    var signIn by rememberSaveable {
        mutableStateOf(true)
    }
    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                coroutineScope.launch {
                    viewModel.signInWithGoogleIntent(intent = result.data ?: return@launch)
                }
            }
        }
    )

    LaunchedEffect(key1 = signUiState) {
        when {
            signUiState.isSignInSuccessful -> {
                onUserLoggedIn()
            }

            !signUiState.signInError.isNullOrEmpty() -> {
                coroutineScope.launch {
                    isLoading = false
                    signUiState.signInError?.let {
                        snackBarHostState.showSnackbar(it)
                        viewModel.onSignInResult(SignInResult(null, null))
                    }
                }
            }
        }
    }

    LoginScreenContent(
        signIn = signIn,
        isLoading = isLoading,
        credentialValidationResultState = credentialValidationResultState,
        snackBarHostState = snackBarHostState,
        onCredentialChanged = { credential ->
            viewModel.saveCredentialValidationResult(
                areCredentialsValid(
                    credential
                )
            )
        },
        onSignInUsingGoogle = {
            coroutineScope.launch {
                val signInIntentSender = viewModel.signInViaGoogle()
                launcher.launch(
                    IntentSenderRequest.Builder(
                        signInIntentSender ?: return@launch
                    ).build()
                )
            }
        },
        onSignInAnonymously = {
            coroutineScope.launch {
                viewModel.signInAnonymously()
            }
        },
        onCredentialsEntered = { credentials ->
            when (credentials) {
                is Credentials.Sensitive.SignInCredentials -> {
                    viewModel.saveCredentialValidationResult(areCredentialsValid(
                        Credentials.Sensitive.SignInCredentials(
                            email = credentials.email,
                            password = credentials.password
                        )
                    ).also { validationResult ->
                        if (validationResult is CredentialValidationResult.Valid) {
                            isLoading = true

                            coroutineScope.launch {
                                viewModel.signInViaEmailAndPassword(
                                    Credentials.Sensitive.SignInCredentials(
                                        email = credentials.email,
                                        password = credentials.password
                                    )
                                )
                            }
                        }
                    }
                    )
                }

                is Credentials.Sensitive.SignUpCredentials -> {
                    viewModel.saveCredentialValidationResult(areCredentialsValid(
                        credentials
                    ).also { validationResult ->
                        if (validationResult is CredentialValidationResult.Valid) {
                            isLoading = true

                            coroutineScope.launch {
                                viewModel.signUpViaEmailAndPassword(
                                    credentials
                                )
                            }
                        } else if (((validationResult as? CredentialValidationResult.Invalid)?.passwordError
                                    as? PasswordValidationResult.Invalid)?.cause?.contains(
                                PasswordErrorType.PASSWORD_EMPTY
                            ) == true
                        ) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(
                                    message = context.resources.getString(
                                        R.string.fill_all_fields
                                    )
                                )
                            }
                        }
                    }
                    )
                }

                else -> {

                }
            }
        },
        onSignInAnnotatedStringClick = {
            signIn = !signIn
        }
    )
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreenContent(
        signIn = true,
        isLoading = false,
        credentialValidationResultState = null,
        snackBarHostState = SnackbarHostState(),
        onCredentialChanged = {},
        onSignInUsingGoogle = {},
        onSignInAnonymously = {},
        onCredentialsEntered = {},
        onSignInAnnotatedStringClick = {}
    )
}