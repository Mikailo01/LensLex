package com.bytecause.lenslex.ui.screens

import android.app.Activity
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.ui.interfaces.SimpleResult
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.PasswordFields
import com.bytecause.lenslex.ui.components.UserAuthBackground
import com.bytecause.lenslex.ui.components.UserAuthBackgroundExpanded
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.LocalOrientationMode
import com.bytecause.lenslex.util.OrientationMode
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import com.bytecause.lenslex.util.shimmerEffect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun UpdatePasswordScreenContent(
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    password: String,
    confirmationPassword: String,
    snackBarHostState: SnackbarHostState,
    yTextOffset: Animatable<Float, AnimationVector1D>,
    yImageOffset: Animatable<Float, AnimationVector1D>,
    passwordErrors: List<PasswordErrorType>,
    isPasswordVisible: Boolean,
    isLoading: Boolean,
    isCodeValid: Boolean?,
    showOobCodeExpiredDialog: Boolean?,
    onPasswordVisibilityClick: (Boolean) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onConfirmPasswordValueChange: (String) -> Unit,
    onCredentialChanged: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onTryAgainClick: () -> Unit,
    onGetNewResetCodeClick: () -> Unit,
    onDismiss: () -> Unit
) {

    if (!isExpandedScreen && LocalOrientationMode.invoke() != OrientationMode.Landscape) {
        UserAuthBackground(
            modifier = modifier.padding(top = 70.dp),
            snackBarHostState = snackBarHostState,
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
                        if (isLoading) {
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
                                password = password,
                                confirmPassword = confirmationPassword,
                                isPasswordEnabled = isCodeValid == true,
                                isPasswordVisible = isPasswordVisible,
                                passwordErrors = passwordErrors,
                                onPasswordValueChange = { onPasswordValueChange(it) },
                                onConfirmPasswordValueChange = { onConfirmPasswordValueChange(it) },
                                onPasswordVisibilityClick = { onPasswordVisibilityClick(!isPasswordVisible) },
                                onCredentialChanged = {
                                    onCredentialChanged()
                                }
                            )

                            Button(
                                onClick = {
                                    if (isCodeValid == true) onResetPasswordClick()
                                    else onTryAgainClick()
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
                                if (isCodeValid == true) Text(text = stringResource(id = R.string.reset_password))
                                else Text(text = stringResource(id = R.string.try_again))
                            }
                        }
                    }

                    if (showOobCodeExpiredDialog == true) {
                        Dialog(
                            title = stringResource(id = R.string.reset_code_expired),
                            onDismiss = { onDismiss() }) {
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
                                Button(onClick = { onGetNewResetCodeClick() }) {
                                    Text(text = stringResource(id = R.string.get_code))
                                }
                            }
                        }
                    }
                }
            }
        )
    } else {
        UserAuthBackgroundExpanded(
            snackBarHostState = snackBarHostState,
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
                        if (isLoading) {
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
                                password = password,
                                confirmPassword = confirmationPassword,
                                isPasswordEnabled = isCodeValid == true,
                                isPasswordVisible = isPasswordVisible,
                                passwordErrors = passwordErrors,
                                onPasswordValueChange = { onPasswordValueChange(it) },
                                onConfirmPasswordValueChange = { onConfirmPasswordValueChange(it) },
                                onPasswordVisibilityClick = { onPasswordVisibilityClick(!isPasswordVisible) },
                                onCredentialChanged = {
                                    onCredentialChanged()
                                }
                            )

                            Button(
                                onClick = {
                                    if (isCodeValid == true) onResetPasswordClick()
                                    else onTryAgainClick()
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
                                if (isCodeValid == true) Text(text = stringResource(id = R.string.reset_password))
                                else Text(text = stringResource(id = R.string.try_again))
                            }
                        }
                    }

                    if (showOobCodeExpiredDialog == true) {
                        Dialog(
                            title = stringResource(id = R.string.reset_code_expired),
                            onDismiss = { onDismiss() }) {
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
                                Button(onClick = { onGetNewResetCodeClick() }) {
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
    val credentialValidationResult by viewModel.credentialValidationResultState.collectAsStateWithLifecycle()

    var password by rememberSaveable {
        mutableStateOf("")
    }
    var confirmPassword by rememberSaveable {
        mutableStateOf("")
    }
    var passwordErrors by rememberSaveable {
        mutableStateOf(emptyList<PasswordErrorType>())
    }
    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    var animationStarted by rememberSaveable {
        mutableStateOf(false)
    }

    val yTextOffset by remember {
        mutableStateOf(Animatable(if (!animationStarted) -700f else 0f))
    }

    val yImageOffset by remember {
        mutableStateOf(Animatable(if (!animationStarted) 700f else 0f))
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = viewModel.resetState) {
        when (viewModel.resetState) {
            SimpleResult.OnSuccess -> {
                snackBarHostState.showSnackbar(context.resources.getString(R.string.password_reset_success))
                viewModel.updateState(null)
                onPasswordChangedSuccess()
            }

            is SimpleResult.OnFailure -> {
                snackBarHostState.showSnackbar((viewModel.resetState as SimpleResult.OnFailure).exception?.message.toString())
                viewModel.updateState(null)
            }

            null -> {
                // init state, do nothing
            }
        }
    }

    LaunchedEffect(viewModel.codeValidationResultState) {
        if (viewModel.codeValidationResultState?.isLoading == false) {
            when (viewModel.codeValidationResultState?.error) {
                UpdatePasswordViewModel.NetworkErrorType.NetworkUnavailable -> {
                    snackBarHostState.showSnackbar(context.resources.getString(R.string.network_unavailable))
                }

                UpdatePasswordViewModel.NetworkErrorType.ServiceUnavailable -> {
                    snackBarHostState.showSnackbar(context.resources.getString(R.string.service_unavailable))
                }

                else -> {
                    // error is null, do nothing
                }
            }
        }
    }

    LaunchedEffect(
        key1 = password,
        key2 = confirmPassword
    ) {
        passwordErrors = when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                when (val passwordError =
                    (credentialValidationResult as CredentialValidationResult.Invalid).passwordError) {
                    is PasswordValidationResult.Invalid -> {
                        passwordError.cause
                    }

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }

    LaunchedEffect(Unit) {
        if (!animationStarted) {
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
                animationStarted = true

                // After animations finish, make API call
                oobCode?.let {
                    viewModel.verifyOob(oobCode = it)
                }
            }
        }
    }

    UpdatePasswordScreenContent(
        isExpandedScreen = isExpandedScreen,
        password = password,
        confirmationPassword = confirmPassword,
        snackBarHostState = snackBarHostState,
        passwordErrors = passwordErrors,
        isPasswordVisible = isPasswordVisible,
        isLoading = viewModel.codeValidationResultState?.isLoading == true,
        isCodeValid = viewModel.codeValidationResultState?.result is UpdatePasswordViewModel.CodeValidation.Valid
                && viewModel.codeValidationResultState?.error == null,
        showOobCodeExpiredDialog = viewModel.codeValidationResultState?.result is UpdatePasswordViewModel.CodeValidation.Invalid,
        yTextOffset = yTextOffset,
        yImageOffset = yImageOffset,
        onPasswordVisibilityClick = { isPasswordVisible = it },
        onPasswordValueChange = { password = it },
        onConfirmPasswordValueChange = { confirmPassword = it },
        onCredentialChanged = {
            viewModel.saveCredentialValidationResult(
                ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.PasswordCredential(
                        password, confirmPassword
                    )
                )
            )
        },
        onResetPasswordClick = {
            if (password.isBlank()) {
                passwordErrors = listOf(PasswordErrorType.PASSWORD_EMPTY)
                return@UpdatePasswordScreenContent
            }

            viewModel.saveCredentialValidationResult(
                ValidationUtil.areCredentialsValid(
                    Credentials.Sensitive.PasswordCredential(
                        password,
                        confirmPassword
                    )
                ).also { validationResult ->
                    if (validationResult is CredentialValidationResult.Valid) {
                        oobCode?.let { code ->
                            viewModel.resetPassword(code, password)
                        }
                    }
                }
            )
        },
        onTryAgainClick = {
            oobCode?.let {
                viewModel.verifyOob(oobCode = it)
            }
        },
        onGetNewResetCodeClick = {
            viewModel.resetCodeValidationState()
            onGetNewResetCodeClick()
        },
        onDismiss = { (context as? Activity)?.finish() }
    )
}

@Composable
@Preview(showBackground = true)
fun UpdatePasswordScreenContentPreview() {
    UpdatePasswordScreenContent(
        isExpandedScreen = false,
        password = "",
        confirmationPassword = "",
        snackBarHostState = remember {
            SnackbarHostState()
        },
        passwordErrors = emptyList(),
        isPasswordVisible = false,
        isLoading = false,
        isCodeValid = true,
        showOobCodeExpiredDialog = false,
        yTextOffset = remember {
            Animatable(0f)
        },
        yImageOffset = remember {
            Animatable(0f)
        },
        onPasswordVisibilityClick = {},
        onPasswordValueChange = {},
        onConfirmPasswordValueChange = {},
        onCredentialChanged = {},
        onResetPasswordClick = {},
        onTryAgainClick = {},
        onGetNewResetCodeClick = {},
        onDismiss = {}
    )
}