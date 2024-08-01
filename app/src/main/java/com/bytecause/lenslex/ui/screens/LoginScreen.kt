package com.bytecause.lenslex.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.remote.auth.abstraction.CredentialManager
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.components.ImageResource
import com.bytecause.lenslex.ui.components.LoginOptionRow
import com.bytecause.lenslex.ui.components.SignIn
import com.bytecause.lenslex.ui.components.SignUp
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.events.LoginUiEffect
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.models.SignInResult
import com.bytecause.lenslex.ui.screens.uistate.LoginState
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.util.TestTags
import com.bytecause.lenslex.util.Util.withOrientationLocked
import com.bytecause.lenslex.util.shadowCustom
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin


@Composable
fun LoginScreenContent(
    isExpandedScreen: Boolean,
    state: LoginState,
    xTextOffset: Animatable<Float, AnimationVector1D>,
    xText2offset: Animatable<Float, AnimationVector1D>,
    onEvent: (LoginUiEvent) -> Unit
) {
    if (isExpandedScreen) {
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
    } else {
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

    val credentialManager: CredentialManager =
        getKoin().get()

    val xTextOffset by remember {
        mutableStateOf(Animatable(if (!uiState.animationFinished) -1050f else 0f))
    }
    val xText2offset by remember {
        mutableStateOf(Animatable(if (!uiState.animationFinished) 1050f else 0f))
    }

    if (uiState.shouldShowCredentialManager) {
        LaunchedEffect(Unit) {
            // Prevention from multiple Credential Manager calls.
            if (uiState.credentialManagerShown) return@LaunchedEffect

            withOrientationLocked(context) {
                viewModel.uiEventHandler(LoginUiEvent.OnCredentialManagerShown)
                val passwordCredential = credentialManager.getCredential(context) ?: run {
                    viewModel.uiEventHandler(LoginUiEvent.OnCredentialManagerDismiss)
                    return@withOrientationLocked
                }

                viewModel.uiEventHandler(
                    LoginUiEvent.OnSignInUsingEmailAndPassword(
                        Credentials.Sensitive.SignInCredentials(
                            email = passwordCredential.id,
                            password = passwordCredential.password
                        )
                    )
                )
            }
        }
    }

    LaunchedEffect(key1 = uiState.signInState) {
        when {
            uiState.signInState.isSignInSuccessful -> {
                keyboardController?.hide()
                // Show save password prompt after successful registration
                if (!uiState.signIn) {
                    credentialManager.saveCredential(context, uiState.email, uiState.password)
                }
                onUserLoggedIn()
            }

            !uiState.signInState.signInError.isNullOrEmpty() -> {
                keyboardController?.hide()
                uiState.signInState.signInError?.let {
                    uiState.snackbarHostState.showSnackbar(it)
                    viewModel.uiEventHandler(
                        LoginUiEvent.OnUpdateSignInResult(
                            SignInResult(
                                null,
                                null
                            )
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginUiEffect.SignInUsingGoogleIntent -> {
                    withOrientationLocked(context) {
                        FirebaseAuthClient().signInUsingGoogleCredential(context).firstOrNull()
                            ?.let { result ->
                                viewModel.uiEventHandler(LoginUiEvent.OnUpdateSignInResult(result))
                            }
                    }
                }

                is LoginUiEffect.NavigateTo -> {
                    onNavigate(effect.destination)
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (!uiState.animationFinished) {
            coroutineScope {
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
            }.invokeOnCompletion { viewModel.uiEventHandler(LoginUiEvent.OnAnimationFinished) }
        }
    }

    LoginScreenContent(
        isExpandedScreen = isExpandedScreen,
        state = uiState,
        xTextOffset = xTextOffset,
        xText2offset = xText2offset,
        onEvent = viewModel::uiEventHandler
    )
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