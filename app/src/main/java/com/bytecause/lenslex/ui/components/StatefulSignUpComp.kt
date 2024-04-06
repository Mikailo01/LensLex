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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.bytecause.lenslex.ui.theme.disabledBorderColor
import com.bytecause.lenslex.ui.theme.focusedBorderColor
import com.bytecause.lenslex.ui.theme.red
import com.bytecause.lenslex.ui.theme.unfocusedBorderColor

@Composable
fun StatefulSignUpComp(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?,
    isLoading: Boolean,
    onSignUpButtonClicked: (Credentials.SignUpCredentials) -> Unit,
    onCredentialChanged: (Credentials.SignUpCredentials) -> Unit,
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
        mutableStateOf(false)
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
                            passwordError.cause.all {
                                it == PasswordErrorType.PASSWORD_MISMATCH || it == PasswordErrorType.PASSWORD_INCORRECT
                            }
                        }

                        else -> false
                    }
            }

            else -> {
                isEmailError = false
                isPasswordError = false
            }
        }
    }

    /*LaunchedEffect(
        key1 = password,
        key2 = confirmPassword,
        key3 = (credentialValidationResult as? CredentialValidationResult.Invalid)?.isEmailValid
    ) {
        if (credentialValidationResult is CredentialValidationResult.Invalid) {
            if (credentialValidationResult.passwordError is PasswordValidationResult.Invalid) {
                isPasswordError = credentialValidationResult.passwordError.cause.none { errorType ->
                    errorType != PasswordErrorType.PASSWORD_MISMATCH
                }
            }
            isEmailError = credentialValidationResult.isEmailValid == false
        } else {
            isPasswordError = false
            isEmailError = false
        }
    }*/

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
                    Credentials.SignUpCredentials(
                        email, password, confirmPassword
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
                    contentDescription = stringResource(id = R.string.email),
                    tint = Color.Black
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = isEmailError,
            supportingText = {
                if (isEmailError) {
                    Text(text = stringResource(id = R.string.email_unsupported_format_warning))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = disabledBorderColor
            )
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            enabled = !(email.isBlank() || isEmailError),
            onValueChange = {
                password = it
                onCredentialChanged(
                    Credentials.SignUpCredentials(
                        email, password, confirmPassword
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
                    contentDescription = stringResource(id = R.string.password),
                    tint = Color.Black
                )
            },
            trailingIcon = {
                val iconId =
                    if (!isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
                val contentDescription =
                    if (!isPasswordVisible) R.string.password_hidden else R.string.password_shown
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                }
                ) {
                    Image(
                        painterResource(id = iconId),
                        contentDescription = stringResource(id = contentDescription)
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError && password.isBlank()) {
                    Text(text = stringResource(id = R.string.password_empty_warning))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = disabledBorderColor
            )
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = confirmPassword,
            enabled = password.isNotEmpty(),
            onValueChange = {
                confirmPassword = it
                onCredentialChanged(
                    Credentials.SignUpCredentials(
                        email, password, confirmPassword
                    )
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.confirm_password),
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_repeat_24),
                    contentDescription = stringResource(id = R.string.confirm_password),
                    tint = Color.Black
                )
            },
            trailingIcon = {
                val iconId =
                    if (!isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
                val contentDescription =
                    if (!isPasswordVisible) R.string.password_hidden else R.string.password_shown
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                }
                ) {
                    Image(
                        painterResource(id = iconId),
                        contentDescription = stringResource(id = contentDescription)
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError) Text(
                    text = stringResource(id = R.string.password_mismatch),
                    color = Color.Red
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = disabledBorderColor
            )
        )

        if (password.isNotEmpty()) {
            PasswordRules(
                modifier = Modifier.padding(10.dp),
                credentialValidationResult = credentialValidationResult
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp)
                /*.animatedBorder(
                    borderColors = listOf(animatePurple, animateBlue),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    borderWidth = 4.dp)*/,
            onClick = {
                onSignUpButtonClicked(
                    Credentials.SignUpCredentials(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                )
            }
        ) {
            if (isLoading) IndeterminateCircularIndicator(isShowed = isLoading)
            else Text(text = stringResource(id = R.string.sign_up))
        }

        AnnotatedClickableText(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            normalText = R.string.sign_prompt,
            annotatedText = R.string.sign_in,
            annotatedTextColor = red,
            onAnnotatedTextClick = {
                onSignInAnnotatedStringClick()
            }
        )
    }
}