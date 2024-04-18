package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.theme.disabledBorderColor
import com.bytecause.lenslex.ui.theme.focusedBorderColor
import com.bytecause.lenslex.ui.theme.unfocusedBorderColor
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType

@Composable
fun PasswordFields(
    credentialValidationResult: CredentialValidationResult?,
    password: String,
    confirmPassword: String,
    isPasswordEnabled: Boolean,
    isPasswordVisible: Boolean,
   // isPasswordError: Boolean,
    isPasswordError: List<PasswordErrorType?>,
    modifier: Modifier = Modifier,
    onPasswordValueChange: (String) -> Unit,
    onConfirmPasswordValueChange: (String) -> Unit,
    onPasswordVisibilityClick: (Boolean) -> Unit,
    onCredentialChanged: () -> Unit
) {

    Column(modifier = modifier) {
        PasswordField(
            password = password,
            isPasswordError = isPasswordError,
            isPasswordEnabled = isPasswordEnabled,
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityClick = onPasswordVisibilityClick,
            onPasswordValueChange = {
                onPasswordValueChange(it)
            },
            onCredentialChanged = {
                onCredentialChanged()
            }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = confirmPassword,
            enabled = password.isNotBlank(),
            onValueChange = {
                onConfirmPasswordValueChange(it)
                onCredentialChanged()
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
                    onPasswordVisibilityClick(!isPasswordVisible)
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
            isError = isPasswordError.isNotEmpty(),
            supportingText = {
                if (isPasswordError.isNotEmpty() && password.isNotBlank()) Text(
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
    }
}

@Composable
@Preview(showBackground = true)
fun PasswordFieldsPreview() {
    PasswordFields(
        credentialValidationResult = null,
        password = "",
        confirmPassword = "",
        isPasswordEnabled = true,
        isPasswordVisible = false,
        isPasswordError = emptyList(),
        onPasswordValueChange = {},
        onConfirmPasswordValueChange = {},
        onPasswordVisibilityClick = {}
    ) { }
}