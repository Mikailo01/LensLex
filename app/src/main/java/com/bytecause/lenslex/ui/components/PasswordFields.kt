package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.TestTags

@Composable
fun PasswordFields(
    password: String,
    confirmPassword: String,
    isPasswordEnabled: Boolean,
    isPasswordVisible: Boolean,
    passwordErrors: List<PasswordErrorType?>,
    modifier: Modifier = Modifier,
    onPasswordValueChange: (String) -> Unit,
    onConfirmPasswordValueChange: (String) -> Unit,
    onPasswordVisibilityClick: (Boolean) -> Unit,
    onCredentialChanged: () -> Unit
) {

    Column(modifier = modifier) {
        PasswordField(
            password = password,
            passwordErrors = passwordErrors,
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
            enabled = password.isNotBlank() && passwordErrors.all { it == PasswordErrorType.PASSWORD_MISMATCH },
            onValueChange = {
                onConfirmPasswordValueChange(it)
                onCredentialChanged()
            },
            label = {
                Text(
                    text = stringResource(id = R.string.confirm_password),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alpha(0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_repeat_24),
                    contentDescription = stringResource(id = R.string.confirm_password),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = 10.dp,
                bottomEnd = 10.dp
            ),
            isError = passwordErrors.isNotEmpty(),
            supportingText = {
                if (passwordErrors.isNotEmpty() && password.isNotBlank()
                    && passwordErrors.all { it == PasswordErrorType.PASSWORD_MISMATCH }
                ) {
                    Text(
                        text = stringResource(id = R.string.password_mismatch),
                        color = Color.Red
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = disabledBorderColor
            ),
        )

        if (password.isNotEmpty()) {
            PasswordRules(
                modifier = Modifier
                    .padding(10.dp)
                    .testTag(TestTags.PASSWORD_RULES),
                passwordErrors = passwordErrors
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PasswordFieldsPreview() {
    PasswordFields(
        password = "",
        confirmPassword = "",
        isPasswordEnabled = true,
        isPasswordVisible = false,
        passwordErrors = emptyList(),
        onPasswordValueChange = {},
        onConfirmPasswordValueChange = {},
        onPasswordVisibilityClick = {}
    ) { }
}