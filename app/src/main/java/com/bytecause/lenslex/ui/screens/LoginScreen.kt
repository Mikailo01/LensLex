package com.bytecause.lenslex.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.components.AnnotatedClickableText
import com.bytecause.lenslex.ui.components.Divider
import com.bytecause.lenslex.ui.components.EmailField
import com.bytecause.lenslex.ui.components.ImageResource
import com.bytecause.lenslex.ui.components.IndeterminateCircularIndicator
import com.bytecause.lenslex.ui.components.LoginOptionRow
import com.bytecause.lenslex.ui.components.PasswordField
import com.bytecause.lenslex.ui.components.PasswordFields
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import com.bytecause.lenslex.util.shadowCustom
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreenContent(
    isExpandedScreen: Boolean,
    signIn: Boolean,
    isLoading: Boolean,
    credentialValidationResultState: CredentialValidationResult?,
    snackBarHostState: SnackbarHostState,
    xTextOffset: Animatable<Float, AnimationVector1D>,
    xText2offset: Animatable<Float, AnimationVector1D>,
    onCredentialChanged: (Credentials.Sensitive) -> Unit,
    onSignInUsingGoogle: () -> Unit,
    onSignInAnonymously: () -> Unit,
    onCredentialsEntered: (Credentials.Sensitive?) -> Unit,
    onForgetPasswordClick: () -> Unit,
    onSignInAnnotatedStringClick: () -> Unit
) {
    if (!isExpandedScreen && LocalOrientationMode.invoke() != OrientationMode.Landscape) {
        UserAuthBackground(
            snackBarHostState = snackBarHostState,
            backgroundContent = {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(id = R.string.welcome_to_lens_lex),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = xTextOffset.value
                        }
                        .shadowCustom(
                            color = Color.Black.copy(0.6f),
                            offsetX = 0.dp,
                            offsetY = 28.dp,
                            blurRadius = 8.dp
                        ),
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(id = R.string.lets_get_started),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(start = 80.dp)
                        .graphicsLayer {
                            translationX = xText2offset.value
                        }
                        .shadowCustom(
                            color = Color.Black.copy(0.6f),
                            offsetX = 0.dp,
                            offsetY = 28.dp,
                            blurRadius = 8.dp
                        ),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            foregroundContent = {
                if (signIn) {
                    StatefulSignIn(
                        credentialValidationResult = credentialValidationResultState,
                        isLoading = isLoading,
                        onCredentialsEntered = { credentials ->
                            onCredentialsEntered(
                                when (credentials) {
                                    is Credentials.Sensitive.SignInCredentials -> {
                                        Credentials.Sensitive.SignInCredentials(
                                            email = credentials.email,
                                            password = credentials.password
                                        )
                                    }

                                    is Credentials.Sensitive.EmailCredential -> {
                                        Credentials.Sensitive.EmailCredential(credentials.email)
                                    }

                                    else -> {
                                        null
                                    }
                                }
                            )
                        },
                        onCredentialChanged = { credential ->
                            onCredentialChanged(credential)
                        },
                        onForgetPasswordClick = { onForgetPasswordClick() },
                        onSignInAnnotatedStringClick = { onSignInAnnotatedStringClick() }
                    )
                } else StatefulSignUp(
                    credentialValidationResult = credentialValidationResultState,
                    isLoading = isLoading,
                    onSignUpButtonClicked = { signUpCredentials ->
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
        )

    } else {
        UserAuthBackgroundExpanded(
            snackBarHostState = snackBarHostState,
            backgroundContent = {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(id = R.string.welcome_to_lens_lex),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = xTextOffset.value
                        }
                        .shadowCustom(
                            color = Color.Black.copy(0.6f),
                            offsetX = 0.dp,
                            offsetY = 28.dp,
                            blurRadius = 8.dp
                        ),
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(id = R.string.lets_get_started),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(start = 80.dp)
                        .graphicsLayer {
                            translationX = xText2offset.value
                        }
                        .shadowCustom(
                            color = Color.Black.copy(0.6f),
                            offsetX = 0.dp,
                            offsetY = 28.dp,
                            blurRadius = 8.dp
                        ),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            foregroundContent = {
                if (signIn) {
                    StatefulSignIn(
                        credentialValidationResult = credentialValidationResultState,
                        isLoading = isLoading,
                        onCredentialsEntered = { credentials ->
                            onCredentialsEntered(
                                when (credentials) {
                                    is Credentials.Sensitive.SignInCredentials -> {
                                        Credentials.Sensitive.SignInCredentials(
                                            email = credentials.email,
                                            password = credentials.password
                                        )
                                    }

                                    is Credentials.Sensitive.EmailCredential -> {
                                        Credentials.Sensitive.EmailCredential(credentials.email)
                                    }

                                    else -> {
                                        null
                                    }
                                }
                            )
                        },
                        onCredentialChanged = { credential ->
                            onCredentialChanged(credential)
                        },
                        onForgetPasswordClick = { onForgetPasswordClick() },
                        onSignInAnnotatedStringClick = { onSignInAnnotatedStringClick() }
                    )
                } else StatefulSignUp(
                    credentialValidationResult = credentialValidationResultState,
                    isLoading = isLoading,
                    onSignUpButtonClicked = { signUpCredentials ->
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
        )
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    isExpandedScreen: Boolean,
    onNavigate: (NavigationItem) -> Unit,
    onUserLoggedIn: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val signUiState by viewModel.signUiState.collectAsStateWithLifecycle()
    val credentialValidationResultState by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()

    var signIn by rememberSaveable {
        mutableStateOf(true)
    }
    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }

    var credentialManagerShown by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current

    var animationStarted by rememberSaveable { mutableStateOf(false) }

    val xTextOffset by remember {
        mutableStateOf(Animatable(if (!animationStarted) -1050f else 0f))
    }
    val xText2offset by remember {
        mutableStateOf(Animatable(if (!animationStarted) 1050f else 0f))
    }

    // Prevention from multiple Credential Manager calls.
    if (!credentialManagerShown) {
        LaunchedEffect(Unit) {
            // TODO("Uncomment after testing")
            //viewModel.signInWithSavedCredential(context = context)
            credentialManagerShown = true
        }
    }

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

    LaunchedEffect(Unit) {
        if (!animationStarted) {
            kotlinx.coroutines.coroutineScope {
                launch {
                    xTextOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }
                launch {
                    xText2offset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }
            }.invokeOnCompletion { animationStarted = true }
        }
    }

    LoginScreenContent(
        isExpandedScreen = isExpandedScreen,
        signIn = signIn,
        isLoading = isLoading,
        credentialValidationResultState = credentialValidationResultState,
        snackBarHostState = snackBarHostState,
        xTextOffset = xTextOffset,
        xText2offset = xText2offset,
        onCredentialChanged = { credential ->
            viewModel.saveCredentialValidationResult(
                areCredentialsValid(
                    credential
                )
            )
        },
        onSignInUsingGoogle = {
            viewModel.signInUsingGoogleCredential(context)
        },
        onSignInAnonymously = {
            coroutineScope.launch {
                viewModel.signInAnonymously()
            }
        },
        onCredentialsEntered = { credentials ->
            keyboardController?.hide()

            when (credentials) {
                is Credentials.Sensitive.SignInCredentials -> {
                    viewModel.saveCredentialValidationResult(areCredentialsValid(credentials).also { validationResult ->
                        if (validationResult is CredentialValidationResult.Valid) {
                            isLoading = true

                            coroutineScope.launch {
                                viewModel.signInViaEmailAndPasswordIfValid(
                                    context,
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
                    viewModel.saveCredentialValidationResult(
                        areCredentialsValid(credentials).also { validationResult ->
                            if (validationResult is CredentialValidationResult.Valid) {
                                isLoading = true

                                coroutineScope.launch {
                                    viewModel.signUpViaEmailAndPassword(
                                        context,
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
        onForgetPasswordClick = {
            onNavigate(NavigationItem.EmailPasswordReset)
        },
        onSignInAnnotatedStringClick = {
            signIn = !signIn
        }
    )
}

@Composable
fun StatefulSignIn(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?,
    isLoading: Boolean,
    onCredentialsEntered: (Credentials.Sensitive) -> Unit,
    onCredentialChanged: (Credentials.Sensitive) -> Unit,
    onForgetPasswordClick: () -> Unit,
    onSignInAnnotatedStringClick: () -> Unit
) {

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    var isEmailError by rememberSaveable {
        mutableStateOf(false)
    }

    var passwordErrors by rememberSaveable {
        mutableStateOf<List<PasswordErrorType>>(emptyList())
    }

    LaunchedEffect(key1 = credentialValidationResult) {
        when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                isEmailError = credentialValidationResult.isEmailValid != true
                passwordErrors =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            when {
                                PasswordErrorType.PASSWORD_EMPTY in passwordError.cause -> listOf(
                                    PasswordErrorType.PASSWORD_EMPTY
                                )

                                PasswordErrorType.PASSWORD_INCORRECT in passwordError.cause -> listOf(
                                    PasswordErrorType.PASSWORD_INCORRECT
                                )

                                else -> emptyList()
                            }
                        }

                        else -> emptyList()
                    }
            }

            else -> {
                isEmailError = false
                passwordErrors = emptyList()
            }
        }
    }

    Column(modifier = modifier) {
        EmailField(
            emailValue = email,
            isEmailError = isEmailError,
            onEmailValueChanged = {
                email = it
                onCredentialChanged(
                    Credentials.Sensitive.SignInCredentials(
                        email, password
                    )
                )
            }
        )

        PasswordField(
            password = password,
            passwordErrors = passwordErrors,
            isPasswordEnabled = !(email.isBlank() || isEmailError),
            isPasswordVisible = passwordVisible,
            onPasswordVisibilityClick = { passwordVisible = it },
            onPasswordValueChange = {
                password = it
            },
            onCredentialChanged = {
                onCredentialChanged(Credentials.Sensitive.SignInCredentials(email, password))
            }
        )

        Text(
            text = stringResource(id = R.string.forget_password),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 10.dp)
                .clickable { onForgetPasswordClick() }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            onClick = {
                if (credentialValidationResult is CredentialValidationResult.Invalid) return@Button

                onCredentialsEntered(
                    Credentials.Sensitive.SignInCredentials(
                        email, password
                    )
                )
            }
        ) {
            if (isLoading) IndeterminateCircularIndicator(isShowed = true)
            else Text(text = stringResource(id = R.string.sign_in))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_up,
            annotatedTextColor = MaterialTheme.colorScheme.error,
            onAnnotatedTextClick = {
                onSignInAnnotatedStringClick()
            }
        )
    }
}

@Composable
fun StatefulSignUp(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?,
    isLoading: Boolean,
    onSignUpButtonClicked: (Credentials.Sensitive.SignUpCredentials) -> Unit,
    onCredentialChanged: (Credentials.Sensitive.SignUpCredentials) -> Unit,
    onSignInAnnotatedStringClick: () -> Unit
) {

    var email by rememberSaveable {
        mutableStateOf("")
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

    var isEmailError by rememberSaveable {
        mutableStateOf(false)
    }

    var passwordErrors by rememberSaveable {
        mutableStateOf<List<PasswordErrorType?>>(emptyList())
    }

    LaunchedEffect(
        key1 = password,
        key2 = confirmPassword,
        key3 = (credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid
    ) {
        when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                isEmailError = credentialValidationResult.isEmailValid != true
                passwordErrors =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            passwordError.cause
                        }

                        else -> emptyList()
                    }
            }

            else -> {
                isEmailError = false
                passwordErrors = emptyList()
            }
        }
    }

    Column(
        modifier = modifier
            .padding(start = 10.dp, end = 10.dp)
    ) {

        EmailField(
            emailValue = email,
            isEmailError = isEmailError,
            onEmailValueChanged = {
                email = it
                onCredentialChanged(
                    Credentials.Sensitive.SignUpCredentials(
                        email, password, confirmPassword
                    )
                )
            }
        )

        PasswordFields(
            password = password,
            confirmPassword = confirmPassword,
            isPasswordEnabled = !(email.isBlank() || isEmailError),
            isPasswordVisible = isPasswordVisible,
            passwordErrors = passwordErrors,
            onPasswordValueChange = { password = it },
            onConfirmPasswordValueChange = { confirmPassword = it },
            onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
            onCredentialChanged = {
                onCredentialChanged(
                    Credentials.Sensitive.SignUpCredentials(
                        email, password, confirmPassword
                    )
                )
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp),
            onClick = {
                if (credentialValidationResult is CredentialValidationResult.Invalid) return@Button

                onSignUpButtonClicked(
                    Credentials.Sensitive.SignUpCredentials(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                )
            }
        ) {
            if (isLoading) IndeterminateCircularIndicator(isShowed = true)
            else Text(text = stringResource(id = R.string.sign_up))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_in,
            annotatedTextColor = MaterialTheme.colorScheme.error,
            onAnnotatedTextClick = {
                onSignInAnnotatedStringClick()
            }
        )
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreenContent(
        isExpandedScreen = false,
        signIn = true,
        isLoading = false,
        credentialValidationResultState = null,
        snackBarHostState = SnackbarHostState(),
        xTextOffset = remember {
            Animatable(0f)
        },
        xText2offset = remember {
            Animatable(0f)
        },
        onCredentialChanged = {},
        onSignInUsingGoogle = {},
        onSignInAnonymously = {},
        onCredentialsEntered = {},
        onForgetPasswordClick = {},
        onSignInAnnotatedStringClick = {}
    )
}

@Composable
@Preview
fun StatefulSignUpCompPreview() {
    StatefulSignUp(
        credentialValidationResult = null,
        isLoading = false,
        onSignUpButtonClicked = {},
        onCredentialChanged = {},
        onSignInAnnotatedStringClick = {}
    )
}

@Composable
@Preview
fun StatefulSignInCompPreview() {
    StatefulSignIn(
        credentialValidationResult = null,
        isLoading = false,
        onCredentialsEntered = {},
        onCredentialChanged = {},
        onForgetPasswordClick = {},
        onSignInAnnotatedStringClick = {}
    )
}