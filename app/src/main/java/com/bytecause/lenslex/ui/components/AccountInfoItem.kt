package com.bytecause.lenslex.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R

sealed interface AccountInfoType {
    data object Uid : AccountInfoType
    data object UserName : AccountInfoType
    data object CreationDate : AccountInfoType
    data object Email : AccountInfoType
    data object Password : AccountInfoType
}

@Composable
fun AccountInfoItem(
    @DrawableRes leadingIconId: Int,
    @StringRes contentDescriptionId: Int,
    accountInfoType: AccountInfoType,
    userCredential: String?,
    isChangeable: Boolean,
    isAnonymous: Boolean?,
    modifier: Modifier = Modifier,
    showSnackBar: () -> Unit = {},
    onAccountInfoChange: (AccountInfoType) -> Unit = {}
) {
    Row(
        modifier = modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = leadingIconId),
            contentDescription = stringResource(id = contentDescriptionId)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    id = when (accountInfoType) {
                        AccountInfoType.Uid -> {
                            R.string.uid
                        }

                        AccountInfoType.UserName -> {
                            R.string.username
                        }

                        AccountInfoType.CreationDate -> {
                            R.string.creation_date
                        }

                        AccountInfoType.Email -> {
                            R.string.email
                        }

                        AccountInfoType.Password -> {
                            R.string.password
                        }
                    }
                ),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = userCredential.takeIf { it?.isNotBlank() == true }
                    ?: stringResource(id = R.string.not_set),
                style = MaterialTheme.typography.labelSmall
            )
        }
        if (isChangeable) {
            val canBeChanged: Boolean =
                isAnonymous == false || isAnonymous == true && accountInfoType is AccountInfoType.UserName

            Text(
                text = stringResource(id = R.string.change),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clickable {
                        if (canBeChanged) {
                            onAccountInfoChange(accountInfoType)
                        } else showSnackBar()
                    }
                    .alpha(if (canBeChanged) 1f else 0.6f)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AccountInfoItemPreview() {
    AccountInfoItem(
        leadingIconId = R.drawable.baseline_alternate_email_24,
        contentDescriptionId = R.string.account_email,
        accountInfoType = AccountInfoType.Email,
        userCredential = stringResource(id = R.string.preview),
        isChangeable = true,
        isAnonymous = false
    )
}