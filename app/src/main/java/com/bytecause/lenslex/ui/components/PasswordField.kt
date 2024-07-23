package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
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

@Composable
fun PasswordField(
    password: String,
    passwordErrors: List<PasswordErrorType?>,
    isPasswordEnabled: Boolean,
    isPasswordVisible: Boolean,
    onPasswordVisibilityClick: (Boolean) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onCredentialChanged: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = password,
        enabled = isPasswordEnabled,
        onValueChange = {
            onPasswordValueChange(it)
            onCredentialChanged()
        },
        label = {
            Text(
                text = stringResource(id = R.string.password),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(id = R.string.password),
                tint = MaterialTheme.colorScheme.onSurface
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
                    contentDescription = stringResource(id = contentDescription),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
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
        isError = passwordErrors.isNotEmpty(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            disabledBorderColor = disabledBorderColor
        ),
        shape = RoundedCornerShape(
            topStart = 10.dp,
            topEnd = 10.dp,
            bottomStart = 10.dp,
            bottomEnd = 10.dp
        ),
        supportingText = {
            if (!isPasswordEnabled) return@OutlinedTextField

            if (passwordErrors.contains(PasswordErrorType.PASSWORD_EMPTY)) {
                Text(text = stringResource(id = R.string.password_empty_warning))
            } else if (passwordErrors.contains(PasswordErrorType.PASSWORD_INCORRECT)
                && !passwordErrors.contains(PasswordErrorType.PASSWORD_MISMATCH)
            ) {
                Text(text = stringResource(id = R.string.password_incorrect))
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun PasswordFieldPreview() {
    PasswordField(
        password = "",
        passwordErrors = emptyList(),
        isPasswordEnabled = true,
        isPasswordVisible = false,
        onPasswordVisibilityClick = {},
        onPasswordValueChange = {}
    ) { }
}