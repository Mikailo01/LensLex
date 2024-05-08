package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.uistate.LoginState
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordValidationResult

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
            onConfirmPasswordValueChange = { onEvent(LoginUiEvent.OnConfirmPasswordValueChange(it)) },
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
fun SignUpPreview() {
    SignUp(
        state = LoginState(),
        onEvent = {}
    )
}