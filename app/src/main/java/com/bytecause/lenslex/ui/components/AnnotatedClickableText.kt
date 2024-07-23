package com.bytecause.lenslex.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.bytecause.lenslex.R

@Composable
fun AnnotatedClickableText(
    modifier: Modifier = Modifier,
    @StringRes normalText: Int,
    @StringRes annotatedText: Int,
    normalTextColor: Color,
    annotatedTextColor: Color,
    textDecoration: TextDecoration = TextDecoration.Underline,
    onAnnotatedTextClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        val str = stringResource(id = normalText, stringResource(id = annotatedText))
        val annotatedSubText = stringResource(id = annotatedText)
        val startIndex = str.indexOf(annotatedSubText)
        val endIndex = startIndex + annotatedSubText.length

        append(str)
        addStyle(
            style = SpanStyle(
                color = annotatedTextColor,
                textDecoration = textDecoration
            ), start = startIndex, end = endIndex
        )
        addStyle(
            style = SpanStyle(
                color = normalTextColor
            ), start = 0, end = startIndex
        )

        addStringAnnotation(
            tag = "clickable",
            annotation = "annotatedElement",
            start = startIndex,
            end = endIndex
        )
    }

    Text(
        text = annotatedString,
        modifier = modifier.clickable {
            // Handle click on annotated text
            annotatedString.getStringAnnotations("clickable", 0, annotatedString.length)
                .firstOrNull()?.let { annotation ->
                    if (annotation.item == "annotatedElement") {
                        onAnnotatedTextClick()
                    }
                }
        },
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
@Preview(showBackground = true)
fun AnnotatedClickableTextPreview() {
    AnnotatedClickableText(
        normalText = R.string.sign_prompt,
        annotatedText = R.string.sign_up,
        normalTextColor = MaterialTheme.colorScheme.onSurface,
        annotatedTextColor = MaterialTheme.colorScheme.error
    ) { }
}