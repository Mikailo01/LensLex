package com.bytecause.lenslex.screens

import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.data.local.SupportedLanguagesLocalDataSource
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.navigation.navhost.popBackStackOnce
import com.bytecause.lenslex.ui.screens.AddScreen
import com.bytecause.lenslex.ui.screens.HomeScreen
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.util.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

class HomeScreenTest {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    // Inject viewModel here instead of in composable to get init state for each test case
    private val viewModel: HomeViewModel by inject(HomeViewModel::class.java)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Home) {
                composable<Screen.Home> {
                    HomeScreen(
                        viewModel = viewModel,
                        isExpandedScreen = false,
                        onClickNavigate = { navController.navigate(Screen.Add) },
                        onPhotoTaken = { _, _ -> }
                    )
                }

                composable<Screen.Add> {
                    AddScreen(
                        onNavigateBack = { navController.popBackStackOnce() }
                    )
                }
            }
        }
    }

    @Test
    fun selectLangOption_isSelected() {
        val supportedLanguages = SupportedLanguagesLocalDataSource.supportedLanguageCodes
        val languages = supportedLanguages.map { Locale(it).displayLanguage }.sortedBy { it }

        // Iterate through each supported language
        languages.forEachIndexed { index, language ->

            // Find and click on node for selecting origin language option
            composeTestRule.onNodeWithTag(TestTags.SELECT_ORIGIN_LANG_OPTION)
                .assertExists()
                .assertIsDisplayed()
                .performClick()

            if (index > 7) {
                // Child item in LazyColumn is not rendered, so scroll must be performed
                composeTestRule.onNode(
                    hasAnyChild(
                        hasTestTag(TestTags.SELECT_ORIGIN_LANG_OPTION).not()
                                and hasTestTag(TestTags.SELECT_TARGET_LANG_OPTION).not()
                                and hasText(languages[0])
                    )
                )
                    .performScrollToIndex(index)
            }

            // Find language child item and perform click
            composeTestRule.onNode(
                hasTestTag(TestTags.SELECT_ORIGIN_LANG_OPTION).not()
                        and hasTestTag(TestTags.SELECT_TARGET_LANG_OPTION).not()
                        and hasText(language)
            )
                .assertExists()
                .assertIsDisplayed()
                .performClick()

            // Ensure the selected language matches the expected one
            composeTestRule.onNodeWithTag(TestTags.SELECT_ORIGIN_LANG_OPTION)
                .assertExists()
                .assertIsDisplayed()
                .assertTextEquals(language)
        }
    }

    @Test
    fun fabButtonClickTest_isOpened() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_options))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Check if correct items are displayed
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.launch_camera))
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.launch_gallery_image_picker))
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.add_new_word_into_the_list))
            .assertExists()
            .assertIsDisplayed()

        // Close FAB options menu
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_options))
            .performClick()
    }

    @Test
    fun word_isAdded_isDeleted_isRestored() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_options))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Navigate to AddScreen
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.add_new_word_into_the_list))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Type new word
        composeTestRule.onNode(hasSetTextAction())
            .assertExists()
            .assertIsDisplayed()
            .performTextInput("hello")

        val supportedLanguages = SupportedLanguagesLocalDataSource.supportedLanguageCodes
        val languages = supportedLanguages.map { Locale(it).displayLanguage }.sortedBy { it }

        // Find and click on node for selecting origin language option
        composeTestRule.onNodeWithTag(TestTags.SELECT_ORIGIN_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText(languages.first())
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.SELECT_ORIGIN_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals(languages.first())

        // Find and click on node for selecting target language option
        composeTestRule.onNodeWithTag(TestTags.SELECT_TARGET_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText(languages[1])
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.SELECT_TARGET_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals(languages[1])

        // Click on "Add" button and save the word
        composeTestRule.onNodeWithText(res.getString(R.string.add))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that the enable edit button is in composition and perform click to enable edit mode
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.enable_edit_mode))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that HomeScreen contains new added word and perform swipeRight touch event, which should delete this word
        composeTestRule.onNodeWithText("hello")
            .assertExists()
            .assertIsDisplayed()
            .performTouchInput {
                swipeRight(0f, 700f)
            }

        // Assert that the word has been successfully deleted
        composeTestRule.onNodeWithText("hello")
            .assertDoesNotExist()

        // Find undo button and perform click
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.undo_changes))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that HomeScreen contains new added word and perform swipeRight touch event, which should delete this word
        composeTestRule.onNodeWithText("hello")
            .assertExists()
            .assertIsDisplayed()
            .performTouchInput {
                swipeRight(0f, 700f)
            }

        // Assert that the word has been successfully deleted
        composeTestRule.onNodeWithText("hello")
            .assertDoesNotExist()

        // Assert that the exit edit mode button is in composition and perform click to finish all changes in the list
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.exit_edit_mode))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that confirm button in dialog is displayed and perform click to completely delete chosen word
        composeTestRule.onNodeWithText(res.getString(R.string.confirm))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that the edit list button is not in composition, because list is empty
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.enable_edit_mode))
            .assertDoesNotExist()
    }
}