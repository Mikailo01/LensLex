package com.bytecause.lenslex.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun AnnotatedClickableText(
    modifier: Modifier = Modifier,
    @StringRes normalText: Int,
    @StringRes annotatedText: Int,
    annotatedTextColor: Color,
    textDecoration: TextDecoration = TextDecoration.Underline,
    onAnnotatedTextClick: () -> Unit
) {

    val annotatedString = buildAnnotatedString {
        val str =
            stringResource(id = normalText, stringResource(id = annotatedText))
        val startIndex = str.indexOf(stringResource(id = annotatedText))
        val endIndex = startIndex + stringResource(id = annotatedText).length
        append(str)
        addStyle(
            style = SpanStyle(
                color = annotatedTextColor,
                textDecoration = textDecoration
            ), start = startIndex, end = endIndex
        )

        // Adding a custom annotation for the clickable segment
        addStringAnnotation(
            tag = "clickable",
            annotation = "annotatedElement",
            start = startIndex,
            end = endIndex
        )
    }

    ClickableText(
        modifier = modifier,
        text = annotatedString,
        onClick = { offset ->
            // Retrieve annotations at the click location
            annotatedString.getStringAnnotations("clickable", offset, offset)
                .firstOrNull()?.let { annotation ->
                    when (annotation.item) {
                        "annotatedElement" -> {
                            // Execute the provided lambda when "Sign up" is clicked
                            onAnnotatedTextClick()
                        }
                    }
                }
        }
    )
}