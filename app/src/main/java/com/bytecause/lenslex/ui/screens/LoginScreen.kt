package com.bytecause.lenslex.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
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
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.ImageResource
import com.bytecause.lenslex.ui.components.LoginOptionRow
import com.bytecause.lenslex.ui.components.SignIn
import com.bytecause.lenslex.ui.components.SignUp
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.models.SignInResult
import com.bytecause.lenslex.ui.screens.uistate.LoginState
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.TestTags
import com.bytecause.lenslex.util.getOrientationMode
import com.bytecause.lenslex.util.shadowCustom
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreenContent(
    isExpandedScreen: Boolean,
    state: LoginState,
    xTextOffset: Animatable<Float, AnimationVector1D>,
    xText2offset: Animatable<Float, AnimationVector1D>,
    onEvent: (LoginUiEvent) -> Unit
) {
    if (!isExpandedScreen && getOrientationMode(LocalConfiguration.current) != OrientationMode.Landscape) {
        UserAuthBackground(
            snackBarHostState = state.snackbarHostState,
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
                        modifier = Modifier.testTag(TestTags.SIGN_IN),
                        onEvent = { onEvent(it) }
                    )
                } else SignUp(
                    state = state,
                    modifier = Modifier.testTag(TestTags.SIGN_UP),
                    onEvent = { onEvent(it) }
                )

                HorizontalDivider(thickness = 2.dp, color = Color.Gray)

                LoginOptionRow(
                    modifier = Modifier.padding(top = 10.dp),
                    optionImage = ImageResource.Painter(painterResource(id = R.drawable.google_logo)),
                    text = stringResource(id = R.string.continue_with_google)
                ) {
                    onEvent(LoginUiEvent.OnSignInUsingGoogle)
                }

                LoginOptionRow(
                    optionImage = ImageResource.ImageVector(Icons.Filled.Person),
                    text = stringResource(id = R.string.continue_anonymously)
                ) {
                    onEvent(LoginUiEvent.OnSignInAnonymously)
                }
            }
        )

    } else {
        UserAuthBackgroundExpanded(
            snackBarHostState = state.snackbarHostState,
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

                HorizontalDivider(thickness = 2.dp, color = Color.Gray)

                LoginOptionRow(
                    modifier = Modifier.padding(top = 10.dp),
                    optionImage = ImageResource.Painter(painterResource(id = R.drawable.google_logo)),
                    text = stringResource(id = R.string.continue_with_google)
                ) {
                    onEvent(LoginUiEvent.OnSignInUsingGoogle)
                }

                LoginOptionRow(
                    optionImage = ImageResource.ImageVector(Icons.Filled.Person),
                    text = stringResource(id = R.string.continue_anonymously)
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
    onNavigate: (Screen) -> Unit,
    onUserLoggedIn: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    var credentialManagerShown by rememberSaveable {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    var animationStarted by rememberSaveable { mutableStateOf(false) }

    val xTextOffset by remember {
        mutableStateOf(Animatable(if (!animationStarted) -1050f else 0f))
    }
    val xText2offset by remember {
        mutableStateOf(Animatable(if (!animationStarted) 1050f else 0f))
    }

    // TODO("Uncomment after testing")
    // Prevention from multiple Credential Manager calls.
    /* if (!credentialManagerShown) {
         LaunchedEffect(Unit) {
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
     }*/

    LaunchedEffect(key1 = uiState.signInState) {
        when {
            uiState.signInState.isSignInSuccessful -> {
                keyboardController?.hide()
                if (!uiState.signIn) {
                    saveCredential(credentialManager, context, uiState.email, uiState.password)
                }
                onUserLoggedIn()
            }

            !uiState.signInState.signInError.isNullOrEmpty() -> {
                keyboardController?.hide()
                coroutineScope.launch {
                    uiState.signInState.signInError?.let {
                        uiState.snackbarHostState.showSnackbar(it)
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
        xTextOffset = xTextOffset,
        xText2offset = xText2offset,
        onEvent = { event ->
            when (event) {
                // Intercept events which have to be handled directly in UI
                is LoginUiEvent.OnForgetPasswordClick -> {
                    onNavigate(Screen.SendEmailPasswordReset)
                }

                is LoginUiEvent.OnSignInUsingGoogle -> {
                    coroutineScope.launch {
                        FirebaseAuthClient().signInUsingGoogleCredential(context).firstOrNull()
                            ?.let { result ->
                                viewModel.onSignInResult(result)
                            }
                    }
                }

                else -> {
                    viewModel.uiEventHandler(event as LoginUiEvent.NonDirect)
                }
            }
        }
    )
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
    } catch (e: CreateCredentialCancellationException) {
        // do nothing, the user chose not to save the credential
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
@Preview
fun LoginScreenPreview() {
    LoginScreenContent(
        isExpandedScreen = false,
        state = LoginState(),
        xTextOffset = remember {
            Animatable(0f)
        },
        xText2offset = remember {
            Animatable(0f)
        },
        onEvent = {}
    )
}