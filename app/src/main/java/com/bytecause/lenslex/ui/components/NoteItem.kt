package com.bytecause.lenslex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bytecause.lenslex.R
import com.bytecause.lenslex.util.introShowcaseBackgroundAlpha
import com.bytecause.lenslex.util.swipeToDismiss
import com.bytecause.lenslex.util.then
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.ShowcaseStyle


@Composable
fun IntroShowcaseScope.NoteItem(
    modifier: Modifier = Modifier,
    originalText: String,
    translatedText: String,
    isEditEnabled: Boolean,
    scale: Float = 1f,
    showIntro: Boolean = false,
    onRemove: () -> Unit,
    onClick: (String) -> Unit
) {
    Column(
        modifier
            .padding(8.dp)
            .then(isEditEnabled, onTrue = { swipeToDismiss { onRemove() } })
    ) {
        Text(
            text = originalText,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .scale(scale)
                // disable onClick callback to prevent interference with swipeToDismiss
                .then(isEditEnabled, onFalse = {
                    clickable {
                        onClick(originalText)
                    }
                })
                .then(showIntro, onTrue = {
                    introShowCaseTarget(
                        index = 9,
                        style = ShowcaseStyle.Default.copy(
                            backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                            backgroundAlpha = introShowcaseBackgroundAlpha,
                            targetCircleColor = Color.White
                        )
                    ) {
                        IntroShowcaseContent(
                            iconRes = R.drawable.text_to_speech,
                            messageRes = R.string.text_to_speech_message
                        )
                    }
                }
                )
        )
        Text(
            text = translatedText,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .scale(scale)
                // disable onClick callback to prevent interference with swipeToDismiss
                .then(isEditEnabled, onFalse = {
                    clickable {
                        onClick(translatedText)
                    }
                })
        )
        HorizontalDivider(thickness = 2.dp, color = Color.Gray)
    }
}