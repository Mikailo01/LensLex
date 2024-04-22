package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.util.CredentialValidationResult


@Composable
fun ForgetPasswordComp(
    email: String,
    isEmailError: Boolean,
    isSendEmailButtonEnabled: Boolean,
    credentialValidationResult: CredentialValidationResult?,
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onEmailValueChanged: (String) -> Unit,
    onCredentialsEntered: () -> Unit
) {

    Column {
        EmailField(
            emailValue = email,
            isEmailError = isEmailError,
            onEmailValueChanged = {
                onEmailValueChanged(it)
            }
        )

        Text(
            text = "Sign in",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onSignInClick() }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp),
            enabled = isSendEmailButtonEnabled,
            onClick = {
                if (credentialValidationResult is CredentialValidationResult.Invalid) return@Button

                onCredentialsEntered()
            }
        ) {
            Text(text = "Send")
        }
    }
}