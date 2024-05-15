package com.bytecause.lenslex.screens

import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.screens.HomeScreen
import com.bytecause.lenslex.util.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.compose.koinViewModel

class HomeScreenTest {

    @get: Rule
    val composeTestRule = createComposeRule()

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun setUp() {
        composeTestRule.setContent {
            HomeScreen(
                viewModel = koinViewModel(),
                onClickNavigate = {},
                onPhotoTaken = { uri1, uri2 -> })
        }
    }

    @Test
    fun select_lang_option() {
        // Find and click on node for selecting language option
        composeTestRule.onNodeWithTag(TestTags.SELECT_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Find and click on node with text Arabic
        composeTestRule.onNodeWithText("Arabic")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that the node's text now contains the name of the selected language
        composeTestRule.onNode(
            hasParent(hasTestTag(TestTags.SELECT_LANG_OPTION)),
            useUnmergedTree = true
        )
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Arabic")

        // Repeat previous steps again for different language
        composeTestRule.onNodeWithTag(TestTags.SELECT_LANG_OPTION)
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Czech")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNode(
            hasParent(hasTestTag(TestTags.SELECT_LANG_OPTION)),
            useUnmergedTree = true
        )
            .assertExists()
            .assertIsDisplayed()
            .assertTextEquals("Czech")
    }

    @Test
    fun fabButtonClickTest_isOpened() {
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.floating_action_button_with_destinations))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

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
}