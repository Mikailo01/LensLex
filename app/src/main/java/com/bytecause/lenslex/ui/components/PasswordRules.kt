package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.theme.valid
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult

@Composable
fun PasswordRules(
    modifier: Modifier = Modifier,
    credentialValidationResult: CredentialValidationResult?
) {

    Column(modifier = modifier.fillMaxWidth()) {
        val passwordRules = stringArrayResource(id = R.array.password_rules)

        RuleItem(
            validationResult = credentialValidationResult,
            errorType = PasswordErrorType.LENGTH_OUT_OF_BOUNDS,
            textRule = passwordRules[0],
            errorIcon = Icons.Filled.Close,
            validIcon = Icons.Filled.Check,
            validColor = MaterialTheme.colorScheme.valid,
            errorColor = MaterialTheme.colorScheme.error
        )

        RuleItem(
            validationResult = credentialValidationResult,
            errorType = PasswordErrorType.MISSING_LOWER_CASE,
            textRule = passwordRules[1],
            errorIcon = Icons.Filled.Close,
            validIcon = Icons.Filled.Check,
            validColor = MaterialTheme.colorScheme.valid,
            errorColor = MaterialTheme.colorScheme.error
        )

        RuleItem(
            validationResult = credentialValidationResult,
            errorType = PasswordErrorType.MISSING_UPPER_CASE,
            textRule = passwordRules[2],
            errorIcon = Icons.Filled.Close,
            validIcon = Icons.Filled.Check,
            validColor = MaterialTheme.colorScheme.valid,
            errorColor = MaterialTheme.colorScheme.error
        )

        RuleItem(
            validationResult = credentialValidationResult,
            errorType = PasswordErrorType.MISSING_DIGIT,
            textRule = passwordRules[3],
            errorIcon = Icons.Filled.Close,
            validIcon = Icons.Filled.Check,
            validColor = MaterialTheme.colorScheme.valid,
            errorColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun RuleItem(
    modifier: Modifier = Modifier,
    validationResult: CredentialValidationResult?,
    errorType: PasswordErrorType,
    textRule: String,
    errorIcon: ImageVector,
    validIcon: ImageVector,
    errorColor: Color,
    validColor: Color
) {

    val isError = validationResult is CredentialValidationResult.Invalid &&
            (validationResult.passwordError is PasswordValidationResult.Invalid) &&
            (validationResult.passwordError.cause.contains(errorType))

    val icon = if (isError) errorIcon else validIcon
    val colorFilter = if (isError) ColorFilter.tint(errorColor) else ColorFilter.tint(validColor)

    Row(
        modifier = modifier.padding(top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.padding(end = 10.dp),
            imageVector = icon,
            colorFilter = colorFilter,
            contentDescription = ""
        )
        Text(
            text = textRule,
            fontSize = 12.sp
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PasswordRulesPreview() {
    PasswordRules(credentialValidationResult = null)
}