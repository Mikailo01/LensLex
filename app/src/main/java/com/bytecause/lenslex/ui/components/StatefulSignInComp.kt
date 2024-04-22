package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult


@Composable
fun StatefulSignInComp(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?,
    isLoading: Boolean,
    forgetPassword: Boolean,
    isSendEmailButtonEnabled: Boolean,
    onCredentialsEntered: (Credentials.Sensitive) -> Unit,
    onCredentialChanged: (Credentials.Sensitive) -> Unit,
    onForgetPasswordClick: () -> Unit,
    onSignInClick: () -> Unit,
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

    Column(
        modifier = modifier
            .padding(start = 10.dp, end = 10.dp)
    ) {

        if (!forgetPassword) {
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
                text = "Forget password?",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onForgetPasswordClick() }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 15.dp),
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
        } else {
            ForgetPasswordComp(
                email = email,
                isEmailError = isEmailError,
                isSendEmailButtonEnabled = isSendEmailButtonEnabled,
                credentialValidationResult = credentialValidationResult,
                onSignInClick = {
                    onSignInClick()
                },
                onEmailValueChanged = {
                    email = it
                    onCredentialChanged(
                        Credentials.Sensitive.EmailCredential(it)
                    )
                },
                onCredentialsEntered = {
                    if (credentialValidationResult is CredentialValidationResult.Invalid) return@ForgetPasswordComp

                    onCredentialsEntered(
                        Credentials.Sensitive.EmailCredential(
                            email
                        )
                    )
                }
            )
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
@Preview
fun StatefulSignInCompPreview() {
    StatefulSignInComp(
        credentialValidationResult = null,
        isLoading = false,
        forgetPassword = false,
        isSendEmailButtonEnabled = true,
        onCredentialsEntered = {},
        onCredentialChanged = {},
        onForgetPasswordClick = {},
        onSignInClick = {},
        onSignInAnnotatedStringClick = {}
    )
}