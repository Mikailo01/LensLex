package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.interfaces.TranslationOption
import com.bytecause.lenslex.util.TestTags
import com.bytecause.lenslex.util.then

@Composable
fun LanguagePreferences(
    modifier: Modifier = Modifier,
    originLangName: String,
    targetLangName: String,
    isSwitchEnabled: Boolean = true,
    onClick: (TranslationOption) -> Unit,
    onSwitchLanguages: () -> Unit = {}
) {
    val alphaValue by remember {
        mutableFloatStateOf(if (isSwitchEnabled) 1f else 0.3f)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundedText(
            modifier = Modifier.padding(start = 5.dp, end = 5.dp),
            text = originLangName,
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.inversePrimary,
            onClick = { onClick(TranslationOption.Origin()) }
        )

        Box(
            modifier = Modifier
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = alphaValue),
                    CircleShape
                )
                .clip(CircleShape)
                .then(
                    isSwitchEnabled,
                    onTrue = { clickable { onSwitchLanguages() } },
                    onFalse = { alpha(alphaValue) })
        ) {
            Icon(
                painter = painterResource(id = R.drawable.switch_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .border(2.dp, MaterialTheme.colorScheme.inversePrimary, CircleShape)
                    .padding(6.dp)
            )
        }

        RoundedText(
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp)
                .testTag(TestTags.SELECT_LANG_OPTION),
            text = targetLangName,
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.inversePrimary,
            onClick = { onClick(TranslationOption.Target()) }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun LanguagePreferencesPreview() {
    LanguagePreferences(
        originLangName = stringResource(id = R.string.preview),
        targetLangName = stringResource(id = R.string.preview),
        onClick = {},
        onSwitchLanguages = {}
    )
}