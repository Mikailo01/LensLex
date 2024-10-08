package com.bytecause.lenslex.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.Dialog
import com.bytecause.lenslex.ui.components.ImageButtonWithText
import com.bytecause.lenslex.ui.components.IntroShowcaseContent
import com.bytecause.lenslex.ui.components.IntroShowcaseText
import com.bytecause.lenslex.ui.components.LanguageDialog
import com.bytecause.lenslex.ui.components.LanguagePreferences
import com.bytecause.lenslex.ui.components.NetworkUnavailableDialog
import com.bytecause.lenslex.ui.components.TopAppBar
import com.bytecause.lenslex.ui.events.ExtractedTextUiEffect
import com.bytecause.lenslex.ui.events.ExtractedTextUiEvent
import com.bytecause.lenslex.ui.mappers.textListToWordList
import com.bytecause.lenslex.ui.models.Word
import com.bytecause.lenslex.ui.screens.model.RecognizedTextState
import com.bytecause.lenslex.ui.screens.viewmodel.ExtractedTextViewModel
import com.bytecause.lenslex.util.introShowcaseBackgroundAlpha
import com.bytecause.lenslex.util.then
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.IntroShowcaseScope
import com.canopas.lib.showcase.component.IntroShowcaseState
import com.canopas.lib.showcase.component.ShowcaseStyle
import com.canopas.lib.showcase.component.rememberIntroShowcaseState
import com.ehsanmsz.mszprogressindicator.progressindicator.BallGridPulseProgressIndicator
import org.koin.androidx.compose.koinViewModel

private const val ROW_WIDTH_SCALE_FACTOR = 0.6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtractedTextScreenContent(
    state: RecognizedTextState,
    introShowcaseState: IntroShowcaseState,
    isExpandedScreen: Boolean,
    onEvent: (ExtractedTextUiEvent) -> Unit
) {
    IntroShowcase(
        showIntroShowCase = state.showIntroShowcase,
        onShowCaseCompleted = { onEvent(ExtractedTextUiEvent.OnShowcaseCompleted) },
        state = introShowcaseState
    ) {
        if (isExpandedScreen) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        titleRes = R.string.result,
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        actionIcons = listOf {
                            ImageButtonWithText(
                                icon = Icons.Filled.Check,
                                iconColor = MaterialTheme.colorScheme.onPrimary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                text = stringResource(id = R.string.select_all),
                                contentDescription = stringResource(id = R.string.select_all),
                                onClick = { onEvent(ExtractedTextUiEvent.OnSelectAllWords) }
                            )
                            ImageButtonWithText(
                                icon = Icons.Filled.Clear,
                                iconColor = MaterialTheme.colorScheme.onPrimary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                text = stringResource(id = R.string.unselect_all),
                                contentDescription = stringResource(id = R.string.unselect_all),
                                onClick = { onEvent(ExtractedTextUiEvent.OnUnselectAllWords) }
                            )
                            IconButton(onClick = { onEvent(ExtractedTextUiEvent.OnHintActionIconClick) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.hint),
                                    contentDescription = stringResource(id = R.string.hint),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        onNavigationIconClick = { onEvent(ExtractedTextUiEvent.OnBackButtonClick) }
                    )
                },
                floatingActionButton = {
                    if (state.selectedWords.isNotEmpty() && !state.isSentence || state.showIntroShowcase) {
                        FloatingActionButton(
                            onClick = { onEvent(ExtractedTextUiEvent.OnFabActionButtonClick) },
                            modifier = Modifier.introShowCaseTarget(
                                index = 4,
                                style = ShowcaseStyle.Default.copy(
                                    backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                    backgroundAlpha = introShowcaseBackgroundAlpha,
                                    targetCircleColor = Color.White
                                )
                            ) {
                                IntroShowcaseContent(
                                    iconRes = R.drawable.goal,
                                    messageRes = R.string.all_words_chosen_showcase_message
                                )
                            }
                        ) {
                            Image(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                    }
                }
            ) { innerPaddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPaddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LanguagePreferences(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(5.dp),
                            originLangName = state.selectedLanguageOptions.first.lang.langName,
                            targetLangName = state.selectedLanguageOptions.second.lang.langName,
                            onClick = { onEvent(ExtractedTextUiEvent.OnShowLanguageDialog(it)) },
                            onSwitchLanguages = { onEvent(ExtractedTextUiEvent.OnSwitchLanguageOptions) }
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isSentence) {
                                SentenceModeLayout(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    state = state,
                                    onEvent = onEvent
                                )
                            }
                            WordsLayout(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                words = state.words,
                                isSentence = state.isSentence,
                                sentence = state.sentence,
                                selectedWords = state.selectedWords,
                                onWordClick = { onEvent(ExtractedTextUiEvent.OnWordClick(it)) },
                                onWordLongClick = { onEvent(ExtractedTextUiEvent.OnWordLongClick(it)) }
                            )
                        }
                    }

                    if (state.showLanguageDialog != null) {
                        LanguageDialog(
                            lazyListContent = state.supportedLanguages,
                            translationOption = state.showLanguageDialog,
                            filterText = state.languageFilterText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(16.dp),
                            onFilterTextChange = {
                                onEvent(
                                    ExtractedTextUiEvent.OnLanguageFilterTextChange(
                                        it
                                    )
                                )
                            },
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnShowLanguageDialog(null)) },
                            onConfirm = {
                                onEvent(ExtractedTextUiEvent.OnConfirmLanguageDialog(it))
                            },
                            onDownload = { langCode ->
                                onEvent(ExtractedTextUiEvent.OnDownloadLanguage(langCode))
                            },
                            onRemove = { langCode ->
                                onEvent(ExtractedTextUiEvent.OnRemoveLanguage(langCode))
                            }
                        )
                    }

                    SnackbarHost(
                        hostState = state.snackbarHostState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )

                    if (state.showNetworkErrorDialog) {
                        NetworkUnavailableDialog(
                            text = stringResource(id = R.string.models_missing),
                            onTryAgainClick = { onEvent(ExtractedTextUiEvent.OnTryAgainClick) },
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnDismissNetworkErrorDialog) })
                    }

                    if (state.showLanguageInferenceErrorDialog) {
                        LanguageSelectionRequiredDialog(
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnDismissLanguageInferenceErrorDialog) }
                        )
                    }

                    if (state.isLoading) {
                        BallGridPulseProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        titleRes = R.string.result,
                        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                        actionIcons = listOf {
                            IconButton(onClick = { onEvent(ExtractedTextUiEvent.OnHintActionIconClick) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.hint),
                                    contentDescription = stringResource(id = R.string.hint),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        },
                        onNavigationIconClick = { onEvent(ExtractedTextUiEvent.OnBackButtonClick) }
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                        onItemClick = { action ->
                            when (action) {
                                BottomAppBarItems.SELECT_ALL -> onEvent(ExtractedTextUiEvent.OnSelectAllWords)
                                BottomAppBarItems.UNSELECT_ALL -> onEvent(ExtractedTextUiEvent.OnUnselectAllWords)
                                BottomAppBarItems.COPY -> onEvent(ExtractedTextUiEvent.OnCopyContent)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (state.selectedWords.isNotEmpty() && !state.isSentence || state.showIntroShowcase) {
                        FloatingActionButton(
                            onClick = { onEvent(ExtractedTextUiEvent.OnFabActionButtonClick) },
                            modifier = Modifier.introShowCaseTarget(
                                index = 4,
                                style = ShowcaseStyle.Default.copy(
                                    backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                    backgroundAlpha = introShowcaseBackgroundAlpha,
                                    targetCircleColor = Color.White
                                )
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.goal),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    IntroShowcaseText(
                                        text = stringResource(id = R.string.all_words_chosen_showcase_message),
                                        modifier = Modifier.padding(top = 30.dp)
                                    )
                                }
                            }
                        ) {
                            Image(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null
                            )
                        }
                    }
                }
            ) { innerPaddingValues ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPaddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        LanguagePreferences(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(5.dp),
                            originLangName = state.selectedLanguageOptions.first.lang.langName,
                            targetLangName = state.selectedLanguageOptions.second.lang.langName,
                            onClick = { onEvent(ExtractedTextUiEvent.OnShowLanguageDialog(it)) },
                            onSwitchLanguages = { onEvent(ExtractedTextUiEvent.OnSwitchLanguageOptions) }
                        )

                        HorizontalDivider()

                        WordsLayout(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            words = state.words,
                            isSentence = state.isSentence,
                            sentence = state.sentence,
                            selectedWords = state.selectedWords,
                            onWordClick = { onEvent(ExtractedTextUiEvent.OnWordClick(it)) },
                            onWordLongClick = { onEvent(ExtractedTextUiEvent.OnWordLongClick(it)) }
                        )

                        if (state.isSentence) {
                            SentenceModeLayout(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                state = state,
                                onEvent = onEvent
                            )
                        }
                    }

                    if (state.showLanguageDialog != null) {
                        LanguageDialog(
                            lazyListContent = state.supportedLanguages,
                            translationOption = state.showLanguageDialog,
                            filterText = state.languageFilterText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(16.dp),
                            onFilterTextChange = {
                                onEvent(
                                    ExtractedTextUiEvent.OnLanguageFilterTextChange(
                                        it
                                    )
                                )
                            },
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnShowLanguageDialog(null)) },
                            onConfirm = {
                                onEvent(ExtractedTextUiEvent.OnConfirmLanguageDialog(it))
                            },
                            onDownload = { langCode ->
                                onEvent(ExtractedTextUiEvent.OnDownloadLanguage(langCode))
                            },
                            onRemove = { langCode ->
                                onEvent(ExtractedTextUiEvent.OnRemoveLanguage(langCode))
                            }
                        )
                    }

                    SnackbarHost(
                        hostState = state.snackbarHostState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )

                    if (state.showNetworkErrorDialog) {
                        NetworkUnavailableDialog(
                            text = stringResource(id = R.string.models_missing),
                            onTryAgainClick = { onEvent(ExtractedTextUiEvent.OnTryAgainClick) },
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnDismissNetworkErrorDialog) })
                    }

                    if (state.showLanguageInferenceErrorDialog) {
                        LanguageSelectionRequiredDialog(
                            onDismiss = { onEvent(ExtractedTextUiEvent.OnDismissLanguageInferenceErrorDialog) }
                        )
                    }

                    if (state.isLoading) {
                        BallGridPulseProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroShowcaseScope.SentenceModeLayout(
    modifier: Modifier = Modifier,
    state: RecognizedTextState,
    onEvent: (ExtractedTextUiEvent) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer
            )
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        Column {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = stringResource(id = R.string.sentence_construction),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(id = R.string.sentence_construction_message),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .heightIn(max = if (maxHeight < maxWidth) maxHeight * 0.4f else maxHeight * 0.2f) // Adjust max height based on available space
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = state.sentence.joinToString(" ") { it.text },
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.introShowCaseTarget(
                                index = 1,
                                style = ShowcaseStyle.Default.copy(
                                    backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                    backgroundAlpha = introShowcaseBackgroundAlpha,
                                    targetCircleColor = Color.White
                                )
                            ) {
                                IntroShowcaseContent(
                                    iconRes = R.drawable.paragraph,
                                    messageRes = R.string.sentence_preview_showcase_message
                                )
                            }
                        )
                    }
                }
            }
            SentenceModeButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                onEvent = onEvent
            )
        }
    }
}


@Composable
private fun IntroShowcaseScope.SentenceModeButtons(
    modifier: Modifier = Modifier,
    onEvent: (ExtractedTextUiEvent) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ImageButtonWithText(
            icon = Icons.Filled.Check,
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            text = stringResource(id = R.string.done),
            contentDescription = stringResource(id = R.string.done),
            modifier = Modifier.introShowCaseTarget(
                index = 2,
                style = ShowcaseStyle.Default.copy(
                    backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                    backgroundAlpha = introShowcaseBackgroundAlpha,
                    targetCircleColor = Color.White
                )
            ) {
                IntroShowcaseContent(
                    iconRes = R.drawable.done,
                    messageRes = R.string.complete_sentence_construction_showcase_message
                )
            },
            onClick = { onEvent(ExtractedTextUiEvent.OnSentenceDone) }
        )

        ImageButtonWithText(
            icon = Icons.Filled.Clear,
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            text = stringResource(id = R.string.cancel),
            contentDescription = stringResource(id = R.string.cancel),
            modifier = Modifier.introShowCaseTarget(
                index = 3,
                style = ShowcaseStyle.Default.copy(
                    backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                    backgroundAlpha = introShowcaseBackgroundAlpha,
                    targetCircleColor = Color.White
                )
            ) {
                IntroShowcaseContent(
                    iconRes = R.drawable.cancel,
                    messageRes = R.string.cancel_sentence_construction_showcase_message
                )
            },
            onClick = { onEvent(ExtractedTextUiEvent.OnSentenceCancelled) }
        )
    }
}

@Composable
fun ExtractedTextScreen(
    viewModel: ExtractedTextViewModel = koinViewModel(),
    text: List<String>,
    isExpandedScreen: Boolean,
    onBackButtonClick: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val joinedText by rememberSaveable {
        mutableStateOf(text.joinToString(System.lineSeparator()))
    }

    val introShowcaseState = rememberIntroShowcaseState()

    val context = LocalContext.current

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEventHandler(ExtractedTextUiEvent.OnShowIntroShowcaseIfNecessary)

        viewModel.effect.collect {
            when (it) {
                ExtractedTextUiEffect.ShowNetworkErrorMessage -> {
                    uiState.snackbarHostState.showSnackbar(context.getString(R.string.network_unavailable))
                }

                ExtractedTextUiEffect.ShowMissingLanguageOptionMessage -> {
                    uiState.snackbarHostState.showSnackbar(context.getString(R.string.target_lang_option_not_selected))
                }

                ExtractedTextUiEffect.ResetIntroShowcaseState -> introShowcaseState.reset()

                ExtractedTextUiEffect.Done -> onDone()
                ExtractedTextUiEffect.CopyContent -> {
                    clipboardManager.setText(AnnotatedString(joinedText))
                }

                ExtractedTextUiEffect.NavigateBack -> onBackButtonClick()
            }
        }
    }

    LaunchedEffect(key1 = uiState.words) {
        if (uiState.words.isEmpty()) {
            viewModel.uiEventHandler(ExtractedTextUiEvent.OnAddWords(textListToWordList(text)))
        }
    }

    ExtractedTextScreenContent(
        state = uiState,
        introShowcaseState = introShowcaseState,
        isExpandedScreen = isExpandedScreen,
        onEvent = viewModel::uiEventHandler
    )
}

@Composable
fun LanguageSelectionRequiredDialog(onDismiss: () -> Unit) {
    Dialog(
        title = stringResource(id = R.string.language_cannot_be_inferred_title),
        onDismiss = onDismiss,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.failed),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = stringResource(id = R.string.language_cannot_be_inferred_message),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WordDisplay(
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
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent
            )
            .combinedClickable(
                onLongClick = { onWordLongClick(word) },
                onClick = { onWordClick(word) }
            )
            .padding(4.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .wrapContentWidth()
    ) {
        Text(text = word.text, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun IntroShowcaseScope.WordsLayout(
    modifier: Modifier = Modifier,
    words: List<Word>,
    isSentence: Boolean,
    sentence: List<Word>,
    selectedWords: Set<Word>,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit
) {
    var screenWidth by remember { mutableIntStateOf(0) }

    Box(modifier = modifier
        .onGloballyPositioned {
            screenWidth = it.size.width
        }
    ) {
        val chunks =
            words.chunkedByWidth(maxWidth = (screenWidth * ROW_WIDTH_SCALE_FACTOR).toInt())

        if (screenWidth > 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                itemsIndexed(chunks, key = { index, _ -> index }) { index, rowWords ->
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
                                modifier = Modifier
                                    .padding(2.dp)
                                    .then(index == 0 && word.id == 0, onTrue = {
                                        introShowCaseTarget(
                                            index = 0,
                                            style = ShowcaseStyle.Default.copy(
                                                backgroundColor = MaterialTheme.colorScheme.inversePrimary,
                                                backgroundAlpha = introShowcaseBackgroundAlpha,
                                                targetCircleColor = Color.White
                                            )
                                        ) {
                                            IntroShowcaseContent(
                                                iconRes = R.drawable.list,
                                                messageRes = R.string.words_layout_showcase_message
                                            )
                                        }
                                    })
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun measureTextWidth(text: String, padding: Dp, style: TextStyle = TextStyle.Default): Int {
    val textMeasurer = rememberTextMeasurer()
    val textWidth = textMeasurer.measure(text, style).size.width
    val paddingPx = with(LocalDensity.current) { padding.toPx() }
    return (textWidth + paddingPx * 2).toInt()
}

// Chunk words into rows which will follow maximum specified width
// returns list in list (the size of the outer list corresponds to the number of rows, inner list
// holds content of the given row)
@Composable
private fun List<Word>.chunkedByWidth(maxWidth: Int): List<List<Word>> {
    val rows = mutableListOf<List<Word>>()
    var currentRow = mutableListOf<Word>()
    var currentRowWidth = 0

    for (word in this) {
        val wordWidth =
            measureTextWidth(text = word.text, padding = 4.dp)
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

private enum class BottomAppBarItems {
    SELECT_ALL,
    UNSELECT_ALL,
    COPY
}

@Composable
private fun BottomAppBar(
    modifier: Modifier = Modifier,
    onItemClick: (BottomAppBarItems) -> Unit
) {
    androidx.compose.material3.BottomAppBar(
        actions = {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .clickable { onItemClick(BottomAppBarItems.SELECT_ALL) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(id = R.string.select_all),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.select_all),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .clickable { onItemClick(BottomAppBarItems.UNSELECT_ALL) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.unselect_all),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.unselect_all),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .wrapContentSize()
                        .clickable { onItemClick(BottomAppBarItems.COPY) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = stringResource(id = R.string.copy_content),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = stringResource(id = R.string.copy),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
@Preview
private fun BottomAppBarPreview() {
    BottomAppBar { }
}

@Composable
@Preview(showBackground = true)
private fun ExtractedTextScreenPreview() {
    ExtractedTextScreenContent(
        state = RecognizedTextState(
            words = stringResource(id = R.string.dummy_text)
                .split(" ")
                .mapIndexed { index, s ->
                    Word(
                        id = index,
                        text = s
                    )
                },
            isSentence = true
        ),
        introShowcaseState = rememberIntroShowcaseState(),
        isExpandedScreen = false,
        onEvent = {}
    )
}

@Composable
@Preview
private fun LanguageSelectionRequiredDialogPreview() {
    LanguageSelectionRequiredDialog(onDismiss = {})
}