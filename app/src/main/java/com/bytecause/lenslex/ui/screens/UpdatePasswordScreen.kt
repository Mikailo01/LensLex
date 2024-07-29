package com.bytecause.lenslex.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.PasswordFields
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEffect
import com.bytecause.lenslex.ui.events.UpdatePasswordUiEvent
import com.bytecause.lenslex.ui.screens.uistate.UpdatePasswordState
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.shimmerEffect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun UpdatePasswordScreenContent(
    isExpandedScreen: Boolean,
    state: UpdatePasswordState,
    modifier: Modifier = Modifier,
    yTextOffset: Animatable<Float, AnimationVector1D>,
    yImageOffset: Animatable<Float, AnimationVector1D>,
    onEvent: (UpdatePasswordUiEvent) -> Unit
) {
    if (isExpandedScreen) {
        UserAuthBackgroundExpanded(
            snackBarHostState = state.snackbarHostState,
            backgroundContent = {
                Text(
                    modifier = Modifier.graphicsLayer {
                        translationY = yTextOffset.value
                    },
                    text = stringResource(id = R.string.reset_password),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.graphicsLayer {
                        translationY = yTextOffset.value
                    },
                    text = stringResource(id = R.string.reset_password_message),
                    style = MaterialTheme.typography.bodyLarge
                )

                Image(
                    modifier = Modifier.graphicsLayer {
                        translationY = yImageOffset.value
                    },
                    painter = painterResource(id = R.drawable.reset_password),
                    contentDescription = stringResource(id = R.string.reset_password)
                )
            },
            foregroundContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Loading placeholders
                        if (state.codeValidationResult?.isLoading == true) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .height(60.dp)
                                        .shimmerEffect()
                                ) {}
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .shimmerEffect()
                                ) {}
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                                    .padding(start = 30.dp, end = 30.dp, bottom = 22.dp)
                                    .shimmerEffect()
                            ) {}
                        } else {
                            PasswordFields(
                                password = state.password,
                                confirmPassword = state.confirmationPassword,
                                isPasswordEnabled = state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid,
                                isPasswordVisible = state.passwordVisible,
                                passwordErrors = ((state.credentialValidationResult as? CredentialValidationResult.Invalid)?.passwordError as? PasswordValidationResult.Invalid)?.cause
                                    ?: emptyList(),
                                onPasswordValueChange = {
                                    onEvent(
                                        UpdatePasswordUiEvent.OnPasswordValueChange(
                                            it
                                        )
                                    )
                                },
                                onConfirmPasswordValueChange = {
                                    onEvent(
                                        UpdatePasswordUiEvent.OnConfirmPasswordValueChange(
                                            it
                                        )
                                    )
                                },
                                onPasswordVisibilityClick = { onEvent(UpdatePasswordUiEvent.OnPasswordVisibilityClick) },
                                onCredentialChanged = {

                                }
                            )

                            Button(
                                onClick = {
                                    if (state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid) onEvent(
                                        UpdatePasswordUiEvent.OnResetPasswordClick
                                    )
                                    else onEvent(UpdatePasswordUiEvent.OnTryAgainClick)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp, bottom = 20.dp),
                                shape = RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 10.dp,
                                    bottomEnd = 10.dp
                                )
                            ) {
                                if (state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid) Text(
                                    text = stringResource(id = R.string.reset_password)
                                )
                                else Text(text = stringResource(id = R.string.try_again))
                            }
                        }
                    }

                    if (state.showOobCodeExpiredDialog) {
                        Dialog(
                            title = stringResource(id = R.string.reset_code_expired),
                            onDismiss = { onEvent(UpdatePasswordUiEvent.OnDismiss) }) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.password_reset_code_expired),
                                    contentDescription = stringResource(id = R.string.reset_code_expired)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(id = R.string.reset_code_expired_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(onClick = { onEvent(UpdatePasswordUiEvent.OnGetNewResetCodeClick) }) {
                                    Text(text = stringResource(id = R.string.get_code))
                                }
                            }
                        }
                    }
                }
            }
        )
    } else {
        UserAuthBackground(
            modifier = modifier.padding(top = 70.dp),
            snackBarHostState = state.snackbarHostState,
            backgroundContent = {
                Text(
                    modifier = Modifier.graphicsLayer {
                        translationY = yTextOffset.value
                    },
                    text = stringResource(id = R.string.reset_password),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.graphicsLayer {
                        translationY = yTextOffset.value
                    },
                    text = stringResource(id = R.string.reset_password_message),
                    style = MaterialTheme.typography.bodyLarge
                )

                Image(
                    modifier = Modifier.graphicsLayer {
                        translationY = yImageOffset.value
                    },
                    painter = painterResource(id = R.drawable.reset_password),
                    contentDescription = stringResource(id = R.string.reset_password)
                )
            },
            foregroundContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Loading placeholders
                        if (state.codeValidationResult?.isLoading == true) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .height(60.dp)
                                        .shimmerEffect()
                                ) {}
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .shimmerEffect()
                                ) {}
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                                    .padding(start = 30.dp, end = 30.dp, bottom = 22.dp)
                                    .shimmerEffect()
                            ) {}
                        } else {
                            PasswordFields(
                                password = state.password,
                                confirmPassword = state.confirmationPassword,
                                isPasswordEnabled = state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid,
                                isPasswordVisible = state.passwordVisible,
                                passwordErrors = ((state.credentialValidationResult as? CredentialValidationResult.Invalid)?.passwordError as? PasswordValidationResult.Invalid)?.cause
                                    ?: emptyList(),
                                onPasswordValueChange = {
                                    onEvent(
                                        UpdatePasswordUiEvent.OnPasswordValueChange(
                                            it
                                        )
                                    )
                                },
                                onConfirmPasswordValueChange = {
                                    onEvent(
                                        UpdatePasswordUiEvent.OnConfirmPasswordValueChange(
                                            it
                                        )
                                    )
                                },
                                onPasswordVisibilityClick = { onEvent(UpdatePasswordUiEvent.OnPasswordVisibilityClick) },
                                onCredentialChanged = {

                                }
                            )

                            Button(
                                onClick = {
                                    if (state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid) onEvent(
                                        UpdatePasswordUiEvent.OnResetPasswordClick
                                    )
                                    else onEvent(UpdatePasswordUiEvent.OnTryAgainClick)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp, bottom = 20.dp),
                                shape = RoundedCornerShape(
                                    topStart = 10.dp,
                                    topEnd = 10.dp,
                                    bottomStart = 10.dp,
                                    bottomEnd = 10.dp
                                )
                            ) {
                                if (state.codeValidationResult?.result is UpdatePasswordViewModel.CodeValidation.Valid) Text(
                                    text = stringResource(id = R.string.reset_password)
                                )
                                else Text(text = stringResource(id = R.string.try_again))
                            }
                        }
                    }

                    if (state.showOobCodeExpiredDialog) {
                        Dialog(
                            title = stringResource(id = R.string.reset_code_expired),
                            onDismiss = { onEvent(UpdatePasswordUiEvent.OnDismiss) }) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.password_reset_code_expired),
                                    contentDescription = stringResource(id = R.string.reset_code_expired)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(id = R.string.reset_code_expired_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = FontStyle.Italic
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(onClick = { onEvent(UpdatePasswordUiEvent.OnGetNewResetCodeClick) }) {
                                    Text(text = stringResource(id = R.string.get_code))
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun UpdatePasswordScreen(
    viewModel: UpdatePasswordViewModel = koinViewModel(),
    isExpandedScreen: Boolean,
    oobCode: String?,
    onPasswordChangedSuccess: () -> Unit,
    onGetNewResetCodeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val yTextOffset by remember {
        mutableStateOf(Animatable(if (!uiState.animationFinished) -700f else 0f))
    }

    val yImageOffset by remember {
        mutableStateOf(Animatable(if (!uiState.animationFinished) 700f else 0f))
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                UpdatePasswordUiEffect.GetNewResetCodeClick -> onGetNewResetCodeClick()
                UpdatePasswordUiEffect.ResetSuccessful -> {
                    // Show Toast instead of Snackbar to perform navigation to LoginScreen instantly
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.password_reset_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    onPasswordChangedSuccess()
                }

                is UpdatePasswordUiEffect.ResetFailure -> {
                    uiState.snackbarHostState.showSnackbar(effect.exception?.message.toString())
                }
            }
        }
    }

    LaunchedEffect(uiState.codeValidationResult) {
        if (uiState.codeValidationResult?.isLoading == false) {
            when (uiState.codeValidationResult?.error) {
                UpdatePasswordViewModel.NetworkErrorType.NetworkUnavailable -> {
                    uiState.snackbarHostState.showSnackbar(context.resources.getString(R.string.network_unavailable))
                }

                UpdatePasswordViewModel.NetworkErrorType.ServiceUnavailable -> {
                    uiState.snackbarHostState.showSnackbar(context.resources.getString(R.string.service_unavailable))
                }

                else -> {
                    // error is null, do nothing
                }
            }
        }
    }

    LaunchedEffect(key1 = uiState.dismissExpiredDialog) {
        if (uiState.dismissExpiredDialog) (context as? Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        if (!uiState.animationFinished) {
            coroutineScope {
                launch {
                    yTextOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }
                launch {
                    yImageOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearEasing
                        )
                    )
                }
            }.invokeOnCompletion {
                viewModel.uiEventHandler(UpdatePasswordUiEvent.OnAnimationFinished)

                // After animations finish, make API call
                oobCode?.let { code ->
                    viewModel.uiEventHandler(UpdatePasswordUiEvent.OnVerifyOob(code))
                }
            }
        }
    }

    UpdatePasswordScreenContent(
        isExpandedScreen = isExpandedScreen,
        state = uiState,
        yTextOffset = yTextOffset,
        yImageOffset = yImageOffset,
        onEvent = viewModel::uiEventHandler
    )
}

@Composable
@Preview(showBackground = true)
fun UpdatePasswordScreenContentPreview() {
    UpdatePasswordScreenContent(
        isExpandedScreen = false,
        state = UpdatePasswordState(),
        yTextOffset = remember {
            Animatable(0f)
        },
        yImageOffset = remember {
            Animatable(0f)
        },
        onEvent = {}
    )
}