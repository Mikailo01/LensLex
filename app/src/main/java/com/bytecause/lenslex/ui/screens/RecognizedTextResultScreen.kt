package com.bytecause.lenslex.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.domain.models.Word
import com.bytecause.lenslex.ui.components.BottomAppBar
import com.bytecause.lenslex.ui.components.BottomAppBarItems
import com.bytecause.lenslex.ui.components.ImageButtonWithText
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.RecognizedTextUiEvent
import com.bytecause.lenslex.ui.screens.uistate.RecognizedTextState
import com.bytecause.lenslex.ui.screens.viewmodel.RecognizedTextViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognizedTextResultScreenContent(
    state: RecognizedTextState,
    onEvent: (RecognizedTextUiEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                titleRes = R.string.result,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                actionIcon = {
                    IconButton(onClick = { onEvent(RecognizedTextUiEvent.OnHintActionIconClick) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.hint),
                            contentDescription = "Hint"
                        )
                    }
                },
                onNavigationIconClick = { onEvent(RecognizedTextUiEvent.OnBackButtonClick) }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                onItemClick = { action ->
                    when (action) {
                        BottomAppBarItems.SELECT_ALL -> onEvent(RecognizedTextUiEvent.OnSelectAllWords)
                        BottomAppBarItems.UNSELECT_ALL -> onEvent(RecognizedTextUiEvent.OnUnselectAllWords)
                        BottomAppBarItems.SHARE -> onEvent(RecognizedTextUiEvent.OnStartShareIntent)
                        BottomAppBarItems.COPY -> onEvent(RecognizedTextUiEvent.OnCopyContent)
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedWords.isNotEmpty()) {
                FloatingActionButton(onClick = { onEvent(RecognizedTextUiEvent.OnFabActionButtonClick) }) {
                    Image(imageVector = Icons.Filled.Check, contentDescription = null)
                }
            }
        }
    ) { innerPaddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LanguagePreferences(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp),
                    originLangName = state.selectedLanguageOptions.first.lang.langName,
                    targetLangName = state.selectedLanguageOptions.second.lang.langName,
                    onClick = {
                        onEvent(RecognizedTextUiEvent.OnShowLanguageDialog(it))
                    }
                )

                HorizontalDivider()

                WordsLayout(
                    words = state.words,
                    isSentence = state.isSentence,
                    sentence = state.sentence,
                    selectedWords = state.selectedWords,
                    onWordClick = { onEvent(RecognizedTextUiEvent.OnWordClick(it)) },
                    onWordLongClick = { onEvent(RecognizedTextUiEvent.OnWordLongClick(it)) }
                )
            }

            if (state.isSentence) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f))
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Image(
                            modifier = Modifier,
                            painter = painterResource(id = R.drawable.paragraph),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sentence mode activated.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Selected words will be assembled into a sentence.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = state.sentence.joinToString(" ") { it.text },
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ImageButtonWithText(
                            icon = Icons.Filled.Check,
                            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            text = stringResource(id = R.string.done),
                            contentDescription = null,
                            onClick = { onEvent(RecognizedTextUiEvent.OnSentenceDone) }
                        )

                        ImageButtonWithText(
                            icon = Icons.Filled.Clear,
                            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            text = stringResource(id = R.string.cancel),
                            contentDescription = null,
                            onClick = { onEvent(RecognizedTextUiEvent.OnSentenceCancelled) }
                        )
                    }
                }
            }

            if (state.showLanguageDialog != null) {
                LanguageDialog(
                    lazyListContent = state.supportedLanguages,
                    translationOption = state.showLanguageDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp),
                    onDismiss = { onEvent(RecognizedTextUiEvent.OnShowLanguageDialog(null)) },
                    onConfirm = {
                        onEvent(RecognizedTextUiEvent.OnConfirmDialog(it))
                    },
                    onDownload = { langCode ->
                        onEvent(RecognizedTextUiEvent.OnDownloadLanguage(langCode))
                    },
                    onRemove = { langCode ->
                        onEvent(RecognizedTextUiEvent.OnRemoveLanguage(langCode))
                    }
                )
            }

        }
    }
}

// Mapping and formatting function
private fun textListToWordList(text: List<String>): List<Word> = text.map { textList ->
    // Split words by whitespaces
    textList.split(
        "\\s+".toRegex()
    )
        .mapNotNull { word ->
            // Filter out all unnecessary symbols
            "[a-zA-Z0-9]+".toRegex().find(word)?.value
        }
        .mapIndexed { index, s -> Word(id = index, text = s.lowercase()) }
}
    .flatten()

@Composable
fun RecognizedTextResultScreen(
    viewModel: RecognizedTextViewModel = koinViewModel(),
    text: List<String>,
    onBackButtonClick: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(initialValue = RecognizedTextState())

    val joinedText by rememberSaveable {
        mutableStateOf(text.joinToString(System.lineSeparator()))
    }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, joinedText)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) onDone()
    }

    LaunchedEffect(key1 = uiState.words) {
        if (uiState.words.isEmpty()) {
            viewModel.uiEventHandler(RecognizedTextUiEvent.OnAddWords(textListToWordList(text)))
        }
    }

    RecognizedTextResultScreenContent(
        state = uiState,
        onEvent = { event ->
            when (event) {
                RecognizedTextUiEvent.OnBackButtonClick -> onBackButtonClick()
                RecognizedTextUiEvent.OnCopyContent -> clipboardManager.setText(
                    AnnotatedString(
                        joinedText
                    )
                )

                RecognizedTextUiEvent.OnStartShareIntent -> context.startActivity(shareIntent)

                else -> viewModel.uiEventHandler(event as RecognizedTextUiEvent.NonDirect)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordDisplay(
    word: Word,
    isSelected: Boolean,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.inversePrimary else Color.Transparent
            )
            .combinedClickable(
                onLongClick = { onWordLongClick(word) },
                onClick = { onWordClick(word) }
            )
            .padding(4.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainer
            )
            .wrapContentWidth()
    ) {
        Text(text = word.text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun measureTextWidth(text: String, style: TextStyle, padding: Dp): Int {
    val textMeasurer = rememberTextMeasurer()
    val textWidth = textMeasurer.measure(text, style).size.width
    val paddingPx = with(LocalDensity.current) { padding.toPx() }
    return (textWidth + paddingPx * 2).toInt()
}

@Composable
fun WordsLayout(
    words: List<Word>,
    isSentence: Boolean,
    sentence: List<Word>,
    selectedWords: Set<Word>,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit
) {
    var screenWidth by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned {
            screenWidth = it.size.width
        }
    ) {
        val chunks = words.chunkedByWidth(maxWidth = (screenWidth / 1.7).toInt())

        if (screenWidth > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(chunks) { rowWords ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        rowWords.forEach { word ->
                            WordDisplay(
                                word = word,
                                isSelected = if (!isSentence) selectedWords.any { it.text == word.text }
                                else sentence.contains(word),
                                onWordClick = onWordClick,
                                onWordLongClick = onWordLongClick,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

//
@Composable
fun List<Word>.chunkedByWidth(maxWidth: Int): List<List<Word>> {
    val rows = mutableListOf<List<Word>>()
    var currentRow = mutableListOf<Word>()
    var currentRowWidth = 0

    for (word in this) {
        val wordWidth =
            measureTextWidth(text = word.text, style = TextStyle.Default, padding = 4.dp)
        if (currentRowWidth + wordWidth > maxWidth) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            currentRowWidth = 0
        }
        currentRow.add(word)
        currentRowWidth += wordWidth
    }

    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }

    return rows
}


/*@Composable
fun WordsLayout(
    words: List<Word>,
    isSentence: Boolean,
    sentence: List<Word>,
    selectedWords: Set<Word>,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit
) {
    var screenWidth by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .onGloballyPositioned {
            screenWidth = it.size.width
        }
    ) {
        if (screenWidth == 0) return

        var rowWidth = 0
        val rowWords = mutableListOf<Word>()
        var outOfBoundsWord: Word? = null

        words.forEach { word ->
            val wordWidth =
                measureTextWidth(text = word.text, style = TextStyle.Default, padding = 4.dp)

            if (rowWidth + wordWidth > screenWidth / 2) {
                // Save word which won't fit in row
                outOfBoundsWord = word

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    rowWords.forEach { rowWord ->
                        WordDisplay(
                            word = rowWord,
                            isSelected = if (!isSentence) selectedWords.any { it.text == rowWord.text }
                            else sentence.contains(rowWord),
                            onWordClick = onWordClick,
                            onWordLongClick = onWordLongClick,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                rowWords.clear()
                rowWidth = 0
            } else {
                // Add word if any
                outOfBoundsWord?.let {
                    rowWords.add(it)
                    outOfBoundsWord = null
                }
                rowWords.add(word)
                rowWidth += wordWidth
            }
        }

        if (rowWords.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                rowWords.forEach { rowWord ->
                    WordDisplay(
                        word = rowWord,
                        isSelected = selectedWords.contains(rowWord),
                        onWordClick = onWordClick,
                        onWordLongClick = onWordLongClick,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}*/

@Composable
@Preview(showBackground = true)
fun RecognizedTextResultScreenPreview() {
    RecognizedTextResultScreenContent(
        state = RecognizedTextState(words = stringResource(id = R.string.dummy_text)
            .split(" ")
            .mapIndexed { index, s ->
                Word(
                    id = index,
                    text = s
                )
            }),
        onEvent = {}
    )
}