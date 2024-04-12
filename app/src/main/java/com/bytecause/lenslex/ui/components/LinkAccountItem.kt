package com.bytecause.lenslex.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


@Composable
fun LinkAccountItem(
    @DrawableRes leadingIcon: Int,
    @StringRes contentDescription: Int,
    accountProvider: String,
    isLinked: Boolean,
    modifier: Modifier = Modifier,
    onLinkButtonClick: () -> Unit
) {
    val transition = updateTransition(isLinked, label = "Update Check/Uncheck transition")

    val iconSize by transition.animateDp(
        transitionSpec = {
            keyframes {
                durationMillis = 300
                0.dp at 50 // Animation starts at 0.dp and reaches 48.dp at 300ms
                24.dp at 300
            }
        }, label = "Check/Uncheck size animation"
    ) {
        if (it) 24.dp else 24.dp
    }

    Row(
        modifier = modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = leadingIcon),
            contentDescription = stringResource(id = contentDescription)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = accountProvider, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (isLinked) "Linked" else "Unlinked",
                style = MaterialTheme.typography.labelSmall
            )
        }
        OutlinedIconButton(
            onClick = { onLinkButtonClick() },
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Box(
                modifier = Modifier.size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(
                    targetState = isLinked,
                    label = "Check/Uncheck Crossfade animation"
                ) { isLinked ->
                    if (isLinked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = Color.Green
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Unchecked",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}
