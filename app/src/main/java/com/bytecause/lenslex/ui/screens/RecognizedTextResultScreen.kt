package com.bytecause.lenslex.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.BottomAppBar
import com.bytecause.lenslex.ui.components.BottomAppBarItems
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognizedTextResultScreenContent(
    text: String,
    onBackButtonClick: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.result,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = { onBackButtonClick() }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                onItemClick = { action ->
                    when (action) {
                        BottomAppBarItems.SHARE -> {
                            context.startActivity(shareIntent)
                        }

                        BottomAppBarItems.COPY -> {
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    }
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                text = text
            )
        }
    }
}

@Composable
fun RecognizedTextResultScreen(
    sharedViewModel: TextRecognitionSharedViewModel,
    onBackButtonClick: () -> Unit
) {
    val text = sharedViewModel.processedTextState.collectAsStateWithLifecycle()

    RecognizedTextResultScreenContent(
        text = text.value.joinToString(System.lineSeparator()),
        onBackButtonClick = { onBackButtonClick() }
    )
}

@Composable
@Preview(showBackground = true)
fun RecognizedTextResultScreenPreview() {

    RecognizedTextResultScreenContent(
        text = stringResource(id = R.string.dummy_text),
        onBackButtonClick = { }
    )
}