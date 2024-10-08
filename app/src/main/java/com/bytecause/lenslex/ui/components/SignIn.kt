package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.ui.screens.model.LoginState
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.TestTags

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
                .clickable { onEvent(LoginUiEvent.OnForgetPasswordClick(Screen.SendEmailPasswordReset)) }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            onClick = { onEvent(LoginUiEvent.OnCredentialsEntered) }
        ) {
            if (state.isLoading) CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            else Text(text = stringResource(id = R.string.sign_in))
        }

        AnnotatedClickableText(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .testTag(TestTags.SIGN_IN_ANNOTATED_STRING),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_up,
            normalTextColor = MaterialTheme.colorScheme.onSurface,
            annotatedTextColor = MaterialTheme.colorScheme.error,
            onAnnotatedTextClick = {
                onEvent(LoginUiEvent.OnAnnotatedStringClick)
            }
        )
    }
}

@Composable
@Preview
fun SignInPreview() {
    SignIn(
        state = LoginState(),
        onEvent = {}
    )
}