package com.bytecause.lenslex.ui.screens

import android.content.Context
import android.util.Log
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
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.uistate.LoginState
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
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.shadowCustom
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreenContent(
    isExpandedScreen: Boolean,
    state: LoginState,
    snackBarHostState: SnackbarHostState,
    xTextOffset: Animatable<Float, AnimationVector1D>,
    xText2offset: Animatable<Float, AnimationVector1D>,
    onEvent: (LoginUiEvent) -> Unit
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
                if (state.signIn) {
                    SignIn(
                        state = state,
                        onEvent = { onEvent(it) }
                    )
                } else SignUp(
                    state = state,
                    onEvent = { onEvent(it) }
                )

                Divider(thickness = 2, color = Color.Gray)

                LoginOptionRow(
                    modifier = Modifier.padding(top = 10.dp),
                    optionImage = ImageResource.Painter(painterResource(id = R.drawable.google_logo)),
                    text = stringResource(id = R.string.continue_with_google),
                    contentDescription = ""
                ) {

                    onEvent(LoginUiEvent.OnSignInUsingGoogle)
                }

                LoginOptionRow(
                    optionImage = ImageResource.ImageVector(Icons.Filled.Person),
                    text = stringResource(id = R.string.continue_anonymously),
                    contentDescription = ""
                ) {
                    onEvent(LoginUiEvent.OnSignInAnonymously)
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
                if (state.signIn) {
                    SignIn(
                        state = state,
                        onEvent = { onEvent(it) }
                    )
                } else SignUp(
                    state = state,
                    onEvent = { onEvent(it) }
                )

                Divider(thickness = 2, color = Color.Gray)

                LoginOptionRow(
                    modifier = Modifier.padding(top = 10.dp),
                    optionImage = ImageResource.Painter(painterResource(id = R.drawable.google_logo)),
                    text = stringResource(id = R.string.continue_with_google),
                    contentDescription = ""
                ) {
                    onEvent(LoginUiEvent.OnSignInUsingGoogle)
                }

                LoginOptionRow(
                    optionImage = ImageResource.ImageVector(Icons.Filled.Person),
                    text = stringResource(id = R.string.continue_anonymously),
                    contentDescription = ""
                ) {
                    onEvent(LoginUiEvent.OnSignInAnonymously)
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
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val signUiState by viewModel.signUiState.collectAsStateWithLifecycle()

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    var credentialManagerShown by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember {
        SnackbarHostState()
    }

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

            try {
                val passwordCredential =
                    getCredential(credentialManager = credentialManager, context = context)
                        ?: return@LaunchedEffect

                viewModel.signInUsingEmailAndPassword(
                    Credentials.Sensitive.SignInCredentials(
                        email = passwordCredential.id,
                        password = passwordCredential.password
                    )
                ).firstOrNull()?.let {
                    viewModel.onSignInResult(it)
                }

            } catch (e: Exception) {
                Log.e("CredentialTest", "Error getting credential", e)
            }

            credentialManagerShown = true
        }
    }

    LaunchedEffect(key1 = signUiState) {
        when {
            signUiState.isSignInSuccessful -> {
                keyboardController?.hide()
                onUserLoggedIn()
            }

            !signUiState.signInError.isNullOrEmpty() -> {
                keyboardController?.hide()
                coroutineScope.launch {
                    //isLoading = false
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
        state = uiState,
        snackBarHostState = snackBarHostState,
        xTextOffset = xTextOffset,
        xText2offset = xText2offset,
        onEvent = {
            when (it) {
                // Handle view events directly inside composable
                is LoginUiEvent.OnForgetPasswordClick -> {
                    onNavigate(NavigationItem.EmailPasswordReset)
                }

                is LoginUiEvent.OnSignInUsingGoogle -> {
                    coroutineScope.launch {
                        viewModel.onSignInResult(
                            FirebaseAuthClient().signInUsingGoogleCredential(
                                context
                            )
                        )
                    }
                }

                else -> {
                    viewModel.uiEventHandler(it)
                }
            }
        }
        /* onCredentialChanged = { credential ->
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
         }*/
    )
}

@Composable
fun SignIn(
    modifier: Modifier = Modifier,
    state: LoginState,
    onEvent: (LoginUiEvent) -> Unit
) {
    Column(modifier = modifier) {
        EmailField(
            emailValue = state.email,
            isEmailError = (state.credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid == false,
            onEmailValueChanged = {
                onEvent(LoginUiEvent.OnEmailValueChange(it))
            }
        )

        PasswordField(
            password = state.password,
            passwordErrors = ((state.credentialValidationResult as? CredentialValidationResult.Invalid)?.passwordError as? PasswordValidationResult.Invalid)?.cause
                ?: emptyList(),
            isPasswordEnabled = !(state.email.isBlank() || (state.credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid == false),
            isPasswordVisible = state.passwordVisible,
            onPasswordVisibilityClick = { onEvent(LoginUiEvent.OnPasswordsVisibilityChange) },
            onPasswordValueChange = {
                onEvent(LoginUiEvent.OnPasswordValueChange(it))
            },
            onCredentialChanged = {

            }
        )

        Text(
            text = stringResource(id = R.string.forget_password),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 10.dp)
                .clickable { onEvent(LoginUiEvent.OnForgetPasswordClick) }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            onClick = { onEvent(LoginUiEvent.OnCredentialsEntered) }
        ) {
            if (state.isLoading) IndeterminateCircularIndicator(isShowed = true)
            else Text(text = stringResource(id = R.string.sign_in))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_up,
            annotatedTextColor = MaterialTheme.colorScheme.error,
            onAnnotatedTextClick = {
                onEvent(LoginUiEvent.OnAnnotatedStringClick)
            }
        )
    }
}

private suspend fun saveCredential(
    credentialManager: CredentialManager,
    context: Context,
    username: String,
    password: String
) {
    try {
        // Ask the user for permission to add the credentials to their store
        credentialManager.createCredential(
            context = context,
            request = CreatePasswordRequest(username, password)
        )
        Log.v("CredentialTest", "Credentials successfully added")
    } catch (e: CreateCredentialCancellationException) {
        // do nothing, the user chose not to save the credential
        Log.v("CredentialTest", "User cancelled the save")
    } catch (e: CreateCredentialException) {
        Log.v("CredentialTest", "Credential save error", e)
    }
}

private suspend fun getCredential(
    credentialManager: CredentialManager,
    context: Context
): PasswordCredential? {
    try {
        // GetPasswordOption() tell the credential library that we're only interested in password credentials
        // Show the user a dialog allowing them to pick a saved credential
        val credentialResponse = credentialManager.getCredential(
            request = GetCredentialRequest(
                listOf(GetPasswordOption())
            ),
            context = context
        )

        // Return the selected credential (as long as it's a username/password)
        return credentialResponse.credential as? PasswordCredential
    } catch (e: GetCredentialCancellationException) {
        // User cancelled the request. Return nothing
        return null
    } catch (e: NoCredentialException) {
        // We don't have a matching credential
        return null
    } catch (e: GetCredentialException) {
        Log.e("CredentialTest", "Error getting credential", e)
        throw e
    }
}

@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    state: LoginState,
    onEvent: (LoginUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .padding(start = 10.dp, end = 10.dp)
    ) {

        EmailField(
            emailValue = state.email,
            isEmailError = (state.credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid == false,
            onEmailValueChanged = {
                onEvent(LoginUiEvent.OnEmailValueChange(it))
            }
        )

        PasswordFields(
            password = state.password,
            confirmPassword = state.confirmPassword,
            isPasswordEnabled = !(state.email.isBlank() || (state.credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid == false),
            isPasswordVisible = state.passwordVisible,
            passwordErrors = ((state.credentialValidationResult as? CredentialValidationResult.Invalid)?.passwordError as? PasswordValidationResult.Invalid)?.cause
                ?: emptyList(),
            onPasswordValueChange = { onEvent(LoginUiEvent.OnPasswordValueChange(it)) },
            onConfirmPasswordValueChange = { onEvent(LoginUiEvent.OnConfirmPasswordChange(it)) },
            onPasswordVisibilityClick = { onEvent(LoginUiEvent.OnPasswordsVisibilityChange) },
            onCredentialChanged = {

            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp),
            onClick = { onEvent(LoginUiEvent.OnCredentialsEntered) }
        ) {
            if (state.isLoading) IndeterminateCircularIndicator(isShowed = true)
            else Text(text = stringResource(id = R.string.sign_up))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_in,
            annotatedTextColor = MaterialTheme.colorScheme.error,
            onAnnotatedTextClick = { onEvent(LoginUiEvent.OnAnnotatedStringClick) }
        )
    }
}

@Composable
@Preview
fun LoginScreenPreview() {
    LoginScreenContent(
        isExpandedScreen = false,
        state = LoginState(),
        snackBarHostState = SnackbarHostState(),
        xTextOffset = remember {
            Animatable(0f)
        },
        xText2offset = remember {
            Animatable(0f)
        },
        onEvent = {}
    )
}

@Composable
@Preview
fun StatefulSignUpCompPreview() {
    SignUp(
        state = LoginState(),
        onEvent = {}
    )
}

@Composable
@Preview
fun StatefulSignInCompPreview() {
    SignIn(
        state = LoginState(),
        onEvent = {}
    )
}