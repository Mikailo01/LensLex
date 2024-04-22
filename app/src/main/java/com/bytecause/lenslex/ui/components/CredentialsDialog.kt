package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult

@Composable
fun CredentialsDialog(
    credentialValidationResult: CredentialValidationResult?,
    modifier: Modifier = Modifier,
    credentialType: CredentialType,
    onDismiss: () -> Unit,
    onEnteredCredential: (Credentials) -> Unit,
    onCredentialChanged: (Credentials.Sensitive) -> Unit
) {

    var username by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var isEmailError by rememberSaveable {
        mutableStateOf(false)
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

    var isPasswordError by rememberSaveable {
        mutableStateOf<List<PasswordErrorType>>(emptyList())
    }

    LaunchedEffect(key1 = credentialValidationResult) {
        isEmailError = when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                credentialValidationResult.isEmailValid != true
            }

            else -> false
        }
    }

    LaunchedEffect(
        key1 = password,
        key2 = confirmPassword
    ) {
        when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                isPasswordError =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            passwordError.cause
                        }

                        else -> emptyList()
                    }
            }

            else -> {
                isPasswordError = emptyList()
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {


                when (credentialType) {
                    is CredentialType.Reauthorization -> {
                        Text(text = stringResource(id = R.string.reauthorization))

                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onCredentialChanged(
                                    Credentials.Sensitive.SignInCredentials(email, password)
                                )
                            }
                        )

                        PasswordField(
                            password = password,
                            passwordErrors = isPasswordError,
                            isPasswordEnabled = !isEmailError,
                            isPasswordVisible = isPasswordVisible,
                            onPasswordVisibilityClick = { isPasswordVisible = it },
                            onPasswordValueChange = {
                                password = it
                            },
                            onCredentialChanged = {
                                onCredentialChanged(
                                    Credentials.Sensitive.SignInCredentials(email, password)
                                )
                            }
                        )

                    }

                    is CredentialType.AccountLink -> {
                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onCredentialChanged(
                                    Credentials.Sensitive.EmailCredential(
                                        it
                                    )
                                )
                            })

                        PasswordFields(
                            credentialValidationResult = credentialValidationResult,
                            password = password,
                            confirmPassword = confirmPassword,
                            isPasswordEnabled = !(email.isBlank() || isEmailError),
                            isPasswordVisible = isPasswordVisible,
                            passwordErrors = isPasswordError,
                            onPasswordValueChange = { password = it },
                            onConfirmPasswordValueChange = { confirmPassword = it },
                            onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                            onCredentialChanged = {
                                onCredentialChanged(
                                    Credentials.Sensitive.PasswordCredential(
                                        password, confirmPassword
                                    )
                                )
                            }
                        )
                    }

                    is CredentialType.Username -> {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_perm_identity_24),
                                    contentDescription = "Enter new username"
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = R.string.username)
                                )
                            })
                    }

                    is CredentialType.Email -> {
                        EmailField(
                            emailValue = email,
                            isEmailError = isEmailError,
                            onEmailValueChanged = {
                                email = it
                                onCredentialChanged(
                                    Credentials.Sensitive.EmailCredential(
                                        it
                                    )
                                )
                            }
                        )
                    }

                    is CredentialType.Password -> {
                        PasswordFields(
                            credentialValidationResult = credentialValidationResult,
                            password = password,
                            confirmPassword = confirmPassword,
                            isPasswordEnabled = true,
                            isPasswordVisible = isPasswordVisible,
                            passwordErrors = isPasswordError,
                            onPasswordValueChange = { password = it },
                            onConfirmPasswordValueChange = { confirmPassword = it },
                            onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                            onCredentialChanged = {
                                onCredentialChanged(
                                    Credentials.Sensitive.PasswordCredential(
                                        password, confirmPassword
                                    )
                                )
                            }
                        )
                    }
                }

                OutlinedButton(onClick = {
                    onEnteredCredential(
                        when (credentialType) {
                            is CredentialType.Reauthorization -> {
                                Credentials.Sensitive.SignInCredentials(email, password)
                            }

                            is CredentialType.AccountLink -> {
                                Credentials.Sensitive.SignInCredentials(email, password)
                            }

                            is CredentialType.Username -> {
                                Credentials.Insensitive.UsernameUpdate(username)
                            }

                            is CredentialType.Email -> {
                                if (email.isBlank()) {
                                    isEmailError = true
                                    return@OutlinedButton
                                }
                                Credentials.Sensitive.EmailCredential(email)
                            }

                            is CredentialType.Password -> {
                                if (password.isBlank()) {
                                    isPasswordError = listOf(PasswordErrorType.PASSWORD_EMPTY)
                                    return@OutlinedButton
                                }
                                Credentials.Sensitive.PasswordCredential(password, confirmPassword)
                            }
                        }
                    )
                }) {
                    Text(text = stringResource(id = R.string.done))
                }
            }
        }
    }
}

sealed interface CredentialType {
    data object Reauthorization : CredentialType
    data object AccountLink : CredentialType
    data object Username : CredentialType
    data object Email : CredentialType
    data object Password : CredentialType
}

@Composable
@Preview(showBackground = true)
fun CredentialsEmailDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Email,
        onDismiss = { },
        onEnteredCredential = {}
    ) { }
}

@Composable
@Preview(showBackground = true)
fun CredentialsPasswordDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Password,
        onDismiss = { },
        onEnteredCredential = {}
    ) { }
}

@Composable
@Preview(showBackground = true)
fun CredentialsUsernameDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Username,
        onDismiss = { },
        onEnteredCredential = {}
    ) { }
}

@Composable
@Preview(showBackground = true)
fun CredentialsReauthorizationDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.Reauthorization,
        onDismiss = { },
        onEnteredCredential = {}
    ) { }
}

@Composable
@Preview(showBackground = true)
fun CredentialsAccountLinkDialogPreview() {
    CredentialsDialog(
        credentialValidationResult = null,
        credentialType = CredentialType.AccountLink,
        onDismiss = { },
        onEnteredCredential = {}
    ) { }
}
