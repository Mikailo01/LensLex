package com.bytecause.lenslex.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.ui.components.CircularProgressWithCount
import com.bytecause.lenslex.ui.components.EmailField
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.screens.viewmodel.SendEmailResetViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.ValidationUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SendEmailResetScreenContent(
    isExpandedScreen: Boolean,
    email: String,
    isEmailError: Boolean,
    timer: Int,
    xOffset: Animatable<Float, AnimationVector1D>,
    yOffset: Animatable<Float, AnimationVector1D>,
    snackBarHostState: SnackbarHostState,
    onEmailValueChanged: (String) -> Unit,
    onSendEmailClick: () -> Unit
) {

    if (!isExpandedScreen && LocalOrientationMode.invoke() != OrientationMode.Landscape) {
        UserAuthBackground(
            snackBarHostState = snackBarHostState,
            backgroundContent = {
                Text(
                    text = stringResource(id = R.string.reset_password_request),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        translationX = xOffset.value
                    }
                )
                Text(
                    text = stringResource(id = R.string.reset_password_request_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.graphicsLayer {
                        translationX = xOffset.value
                    }
                )

                Image(
                    painter = painterResource(id = R.drawable.forgot_password),
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        translationY = yOffset.value
                    }
                )
            },
            foregroundContent = {
                EmailField(
                    emailValue = email,
                    isEmailError = isEmailError,
                    onEmailValueChanged = {
                        onEmailValueChanged(it)
                    })

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { onSendEmailClick() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp),
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        ),
                        enabled = timer == -1,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        if (timer != -1) {
                            CircularProgressWithCount(
                                value = timer.toFloat(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else Text(text = stringResource(id = R.string.send))
                    }
                }
            }
        )
    } else {
        UserAuthBackgroundExpanded(
            snackBarHostState = snackBarHostState,
            backgroundContent = {
                Text(
                    text = stringResource(id = R.string.reset_password_request),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer {
                        translationX = xOffset.value
                    }
                )
                Text(
                    text = stringResource(id = R.string.reset_password_request_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.graphicsLayer {
                        translationX = xOffset.value
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.forgot_password),
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        translationY = yOffset.value
                    }
                )
            },
            foregroundContent = {
                EmailField(
                    emailValue = email,
                    isEmailError = isEmailError,
                    onEmailValueChanged = {
                        onEmailValueChanged(it)
                    })

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { onSendEmailClick() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp),
                        shape = RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        ),
                        enabled = timer == -1,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        if (timer != -1) {
                            CircularProgressWithCount(
                                value = timer.toFloat(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else Text(text = stringResource(id = R.string.send))
                    }
                }
            }
        )
    }
}

@Composable
fun SendEmailResetScreen(
    viewModel: SendEmailResetViewModel = koinViewModel(),
    isExpandedScreen: Boolean
) {
    val requestResult by viewModel.onResetRequestResult.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val credentialValidationResult by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()

    var email by rememberSaveable {
        mutableStateOf("")
    }
    var isEmailError by rememberSaveable {
        mutableStateOf(false)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val context = LocalContext.current

    var animationStarted by rememberSaveable { mutableStateOf(false) }

    val xOffset = remember { Animatable(if (!animationStarted) -1050f else 0f) }
    val yOffset = remember { Animatable(if (!animationStarted) 700f else 0f) }

    LaunchedEffect(key1 = credentialValidationResult) {
        isEmailError = when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                (credentialValidationResult as CredentialValidationResult.Invalid).isEmailValid != true
            }

            else -> false
        }
    }

    LaunchedEffect(Unit) {
        if (!animationStarted) {
            coroutineScope {
                launch {
                    xOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }
                launch {
                    yOffset.animateTo(
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

    LaunchedEffect(key1 = requestResult) {
        when (requestResult) {
            SimpleResult.OnSuccess -> {
                snackBarHostState.showSnackbar(context.resources.getString(R.string.email_sent))
                viewModel.updateResult(null)
            }

            is SimpleResult.OnFailure -> {
                snackBarHostState.showSnackbar(
                    message = (requestResult as SimpleResult.OnFailure)
                        .exception?.message.toString()
                )
                viewModel.updateResult(null)
            }

            null -> {
                // init state, do nothing
            }
        }
    }

    SendEmailResetScreenContent(
        isExpandedScreen = isExpandedScreen,
        email = email,
        isEmailError = isEmailError,
        timer = timer,
        xOffset = xOffset,
        yOffset = yOffset,
        snackBarHostState = snackBarHostState,
        onEmailValueChanged = {
            email = it
            viewModel.saveCredentialValidationResult(
                ValidationUtil.areCredentialsValid(Credentials.Sensitive.EmailCredential(email))
            )
        },
        onSendEmailClick = {
            viewModel.saveCredentialValidationResult(
                ValidationUtil.areCredentialsValid(Credentials.Sensitive.EmailCredential(email))
                    .also { validationResult ->
                        if (validationResult is CredentialValidationResult.Valid) {
                            viewModel.sendPasswordResetEmail(email)
                        }
                    })
        }
    )
}

@Composable
@Preview
fun SendEmailResetScreenContentPreview() {
    SendEmailResetScreenContent(
        isExpandedScreen = false,
        email = "",
        isEmailError = false,
        timer = 50,
        xOffset = remember {
            Animatable(0f)
        },
        yOffset = remember {
            Animatable(0f)
        },
        snackBarHostState = SnackbarHostState(),
        onEmailValueChanged = {},
        onSendEmailClick = {}
    )
}