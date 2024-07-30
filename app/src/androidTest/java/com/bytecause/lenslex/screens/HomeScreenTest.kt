package com.bytecause.lenslex.screens

import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasSetTextAction
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
import com.bytecause.lenslex.util.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale

// TODO("Fix synchronization")
class HomeScreenTest {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun setUp() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = Screen.Home) {
                composable<Screen.Home> {
                    HomeScreen(
                        isExpandedScreen = false,
                        onClickNavigate = { navController.navigate(Screen.Add) },
                        onPhotoTaken = { uri1, uri2 -> }
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

            // Find and click on node for selecting language option
            composeTestRule.onNodeWithTag(TestTags.SELECT_LANG_OPTION)
                .assertExists()
                .assertIsDisplayed()
                .performClick()

            if (index > 7) {
                // Child item in LazyColumn is not rendered, so scroll must be performed
                composeTestRule.onNode(hasAnyChild(hasText(languages[0])))
                    .performScrollToIndex(index)
            }

            // Find language child item and perform click
            composeTestRule.onNodeWithText(language)
                .assertExists()
                .assertIsDisplayed()
                .performClick()

            // Ensure the selected language matches the expected one
            composeTestRule.onNodeWithTag(TestTags.SELECT_LANG_OPTION)
                .assertExists()
                .assertIsDisplayed()
                .assertTextEquals(language)
        }
    }

    @Test
    fun fabButtonClickTest_isOpened() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_destinations))
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
    }

    @Test
    fun addWord_isAdded() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_destinations))
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

        // Click on "Add" button and save the word
        composeTestRule.onNodeWithText(res.getString(R.string.add))
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

        // Assert that the word has been successfully restored
        composeTestRule.onNodeWithText("hello")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun addWordDeleteWord_isDeleted() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_destinations))
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

        // Click on "Add" button and save the word
        composeTestRule.onNodeWithText(res.getString(R.string.add))
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
    }

    @Test
    fun addWordDeleteWordRestoreWord_isRestored() {
        // Find and perform click on FAB button
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_destinations))
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

        // Click on "Add" button and save the word
        composeTestRule.onNodeWithText(res.getString(R.string.add))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        Thread.sleep(1_000)

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

        // Assert that the word has been successfully restored
        composeTestRule.onNodeWithText("hello")
            .assertExists()
            .assertIsDisplayed()
    }
}