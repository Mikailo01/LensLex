package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.Credentials
import com.bytecause.lenslex.ui.screens.viewmodel.CredentialValidationResult
import com.bytecause.lenslex.ui.screens.viewmodel.PasswordErrorType
import com.bytecause.lenslex.ui.screens.viewmodel.PasswordValidationResult


@Composable
fun StatefulSignInComp(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?,
    onCredentialsEntered: (String, String) -> Unit,
    onCredentialChanged: (Credentials.SignInCredentials) -> Unit,
    onSignUpClick: () -> Unit
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

    var isPasswordError by rememberSaveable {
        mutableStateOf<PasswordErrorType?>(null)
    }

    /*LaunchedEffect(key1 = credentialValidationResult) {
        Log.d("idk", "validate")
        isEmailError = if (credentialValidationResult is CredentialValidationResult.Invalid) {
            credentialValidationResult.isEmailValid != true
        } else false
        isPasswordError = if (credentialValidationResult is CredentialValidationResult.Invalid
            && credentialValidationResult.passwordError is PasswordValidationResult.Invalid
        ) {
            credentialValidationResult.passwordError.cause.contains(PasswordErrorType.PASSWORD_EMPTY)
        } else false
    }*/

    LaunchedEffect(key1 = credentialValidationResult) {
        when (credentialValidationResult) {
            is CredentialValidationResult.Invalid -> {
                isEmailError = credentialValidationResult.isEmailValid != true
                isPasswordError =
                    when (val passwordError = credentialValidationResult.passwordError) {
                        is PasswordValidationResult.Invalid -> {
                            when {
                                PasswordErrorType.PASSWORD_EMPTY in passwordError.cause -> PasswordErrorType.PASSWORD_EMPTY
                                PasswordErrorType.PASSWORD_INCORRECT in passwordError.cause -> PasswordErrorType.PASSWORD_INCORRECT
                                else -> null
                            }
                        }

                        else -> null
                    }
            }

            else -> {
                isEmailError = false
                isPasswordError = null
            }
        }
    }


    Column(
        modifier = modifier
            .padding(start = 10.dp, end = 10.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = {
                email = it
                onCredentialChanged(
                    Credentials.SignInCredentials(
                        email = email,
                        password = password
                    )
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.email),
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = stringResource(id = R.string.email)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = isEmailError,
            supportingText = {
                if (isEmailError) Text(text = stringResource(id = R.string.email_unsupported_format_warning))
            }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            enabled = !(email.isBlank() || isEmailError),
            onValueChange = {
                password = it
                onCredentialChanged(
                    Credentials.SignInCredentials(
                        email, password
                    )
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.password),
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(id = R.string.password)
                )
            },
            trailingIcon = {
                val iconId =
                    if (!passwordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
                val contentDescription =
                    if (!passwordVisible) R.string.password_hidden else R.string.password_shown
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }
                ) {
                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = stringResource(id = contentDescription)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = isPasswordError != null,
            supportingText = {
                if (isEmailError) return@OutlinedTextField

                if (isPasswordError == PasswordErrorType.PASSWORD_EMPTY) {
                    Text(text = stringResource(id = R.string.password_empty_warning))
                } else if (isPasswordError == PasswordErrorType.PASSWORD_INCORRECT) {
                    Text(text = "Password incorrect")
                }
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp),
            onClick = {
                onCredentialsEntered(
                    email,
                    password
                )
            }
        ) {
            Text(text = stringResource(id = R.string.sign_in))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_up,
            annotatedTextColor = Color.Red,
            onAnnotatedTextClick = {
                onSignUpClick()
            }
        )
    }
}