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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.bytecause.lenslex.ui.components.CircularProgressWithCount
import com.bytecause.lenslex.ui.components.EmailField
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.events.SendEmailResetUiEffect
import com.bytecause.lenslex.ui.events.SendEmailResetUiEvent
import com.bytecause.lenslex.ui.screens.model.SendEmailResetState
import com.bytecause.lenslex.ui.screens.viewmodel.SendEmailResetViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SendEmailResetScreenContent(
    isExpandedScreen: Boolean,
    state: SendEmailResetState,
    xOffset: Animatable<Float, AnimationVector1D>,
    yOffset: Animatable<Float, AnimationVector1D>,
    onEvent: (SendEmailResetUiEvent) -> Unit
) {
    if (isExpandedScreen) {
        UserAuthBackgroundExpanded(
            showNavigateBackButton = true,
            snackBarHostState = state.snackbarHostState,
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
                    emailValue = state.email,
                    isEmailError = state.isEmailError,
                    onEmailValueChanged = {
                        onEvent(SendEmailResetUiEvent.OnEmailValueChanged(it))
                    })

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { onEvent(SendEmailResetUiEvent.OnSendEmailClick) },
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
                        enabled = state.timer == -1,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        if (state.timer != -1) {
                            CircularProgressWithCount(
                                value = state.timer.toFloat(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else Text(text = stringResource(id = R.string.send))
                    }
                }
            },
            onNavigateBack = { onEvent(SendEmailResetUiEvent.OnNavigateBack) }
        )
    } else {
        UserAuthBackground(
            showNavigateBackButton = true,
            snackBarHostState = state.snackbarHostState,
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
                Box(modifier = Modifier.fillMaxSize()) {
                    EmailField(
                        modifier = Modifier.align(Alignment.TopCenter),
                        emailValue = state.email,
                        isEmailError = state.isEmailError,
                        onEmailValueChanged = {
                            onEvent(SendEmailResetUiEvent.OnEmailValueChanged(it))
                        })

                    Button(
                        onClick = { onEvent(SendEmailResetUiEvent.OnSendEmailClick) },
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
                        enabled = state.timer == -1,
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        if (state.timer != -1) {
                            CircularProgressWithCount(
                                value = state.timer.toFloat(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else Text(text = stringResource(id = R.string.send))
                    }
                }
            },
            onNavigateBack = { onEvent(SendEmailResetUiEvent.OnNavigateBack) }
        )
    }
}

@Composable
fun SendEmailResetScreen(
    viewModel: SendEmailResetViewModel = koinViewModel(),
    isExpandedScreen: Boolean,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val xOffset = remember { Animatable(if (!uiState.animationFinished) -1050f else 0f) }
    val yOffset = remember { Animatable(if (!uiState.animationFinished) 700f else 0f) }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SendEmailResetUiEffect.NavigateBack -> onNavigateBack()
                SendEmailResetUiEffect.SuccessfulRequest -> {
                    uiState.snackbarHostState.showSnackbar(context.resources.getString(R.string.email_sent))
                }

                is SendEmailResetUiEffect.FailureRequest -> {
                    uiState.snackbarHostState.showSnackbar(
                        message = effect.exception?.message.toString()
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.animationFinished) {
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
            }.invokeOnCompletion { viewModel.uiEventHandler(SendEmailResetUiEvent.OnAnimationFinished) }
        }
    }

    SendEmailResetScreenContent(
        isExpandedScreen = isExpandedScreen,
        state = uiState,
        xOffset = xOffset,
        yOffset = yOffset,
        onEvent = viewModel::uiEventHandler
    )
}

@Composable
@Preview
fun SendEmailResetScreenContentPreview() {
    SendEmailResetScreenContent(
        isExpandedScreen = false,
        state = SendEmailResetState(),
        xOffset = remember {
            Animatable(0f)
        },
        yOffset = remember {
            Animatable(0f)
        },
        onEvent = {}
    )
}