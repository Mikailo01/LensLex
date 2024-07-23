package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.theme.disabledBorderColor
import com.bytecause.lenslex.ui.theme.focusedBorderColor
import com.bytecause.lenslex.ui.theme.unfocusedBorderColor

@Composable
fun EmailField(
    emailValue: String,
    isEmailError: Boolean,
    modifier: Modifier = Modifier,
    onEmailValueChanged: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = emailValue,
        onValueChange = {
            onEmailValueChanged(it)
        },
        label = {
            Text(
                text = stringResource(id = R.string.email),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alpha(0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = stringResource(id = R.string.email),
                tint = MaterialTheme.colorScheme.onSurface
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
}

@Composable
@Preview(showBackground = true)
fun EmailFieldPreview() {
    EmailField(emailValue = "", isEmailError = false) { }
}