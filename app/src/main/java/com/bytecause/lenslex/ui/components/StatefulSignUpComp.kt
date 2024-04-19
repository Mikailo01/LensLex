package com.bytecause.lenslex.ui.components

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
fun StatefulSignUpComp(
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

    var isPasswordError by rememberSaveable {
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
                isPasswordError =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            passwordError.cause
                        }

                        else -> emptyList()
                    }
            }

            else -> {
                isEmailError = false
                isPasswordError = emptyList()
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
            onCredentialChanged = {
                email = it
                onCredentialChanged(
                    Credentials.Sensitive.SignUpCredentials(
                        email, password, confirmPassword
                    )
                )
            }
        )

        PasswordFields(
            credentialValidationResult = credentialValidationResult,
            password = password,
            confirmPassword = confirmPassword,
            isPasswordEnabled = !(email.isBlank() || isEmailError),
            isPasswordVisible = isPasswordVisible,
            isPasswordError = isPasswordError,
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
fun StatefulSignUpCompPreview() {
    StatefulSignUpComp(
        credentialValidationResult = null,
        isLoading = false,
        onSignUpButtonClicked = {},
        onCredentialChanged = {},
        onSignInAnnotatedStringClick = {}
    )
}