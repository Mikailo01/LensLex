package com.bytecause.lenslex.screens

import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.MainActivity
import com.bytecause.lenslex.R
import com.bytecause.lenslex.navigation.NavigationItem
import com.bytecause.lenslex.ui.screens.LoginScreen
import com.bytecause.lenslex.ui.theme.AppTheme
import com.bytecause.lenslex.util.TestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get: Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            LoginScreen(isExpandedScreen = false, onNavigate = {}, onUserLoggedIn = {})
            /*val navController = rememberNavController()
            AppTheme {
                NavHost(
                    navController = navController,
                    startDestination = NavigationItem.Login.route
                ) {
                    composable(NavigationItem.Login.route) {
                        LoginScreen(isExpandedScreen = false, onNavigate = {}, onUserLoggedIn = {})
                    }
                }
            }*/
        }
    }

    @Test
    fun assert_email_input_is_equal_to_expected_value() {
        composeTestRule.onNodeWithText(res.getString(R.string.email)).apply {
            assertExists()
            assertIsDisplayed()
            assertIsEnabled()
            performTextClearance()
            performTextInput("example@gmail.com")
            assertTextEquals(res.getString(R.string.email), "example@gmail.com")
        }
    }

    @Test
    fun test_if_password_input_is_equal_to_expected_value_and_toggle_password_visibility_on_off() {
        // Input valid email value to enable password field
        composeTestRule.onNodeWithText(res.getString(R.string.email)).apply {
            assertExists()
            assertIsDisplayed()
            assertIsEnabled()
            performTextClearance()
            performTextInput("example@gmail.com")
        }

        // Password should be enabled
        composeTestRule.onNodeWithText(res.getString(R.string.password))
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()

        // Type a password
        composeTestRule.onNodeWithText(res.getString(R.string.password))
            .assertExists()
            .assertIsDisplayed()
            .assertIsDisplayed()
            .performTextInput("Password123")

        // Check if password visibility toggle button with dynamic content description is displayed,
        // enabled and perform click afterwards
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Check if password field value equals to the expected one
        composeTestRule.onNodeWithText(res.getString(R.string.password))
            .assertTextEquals(res.getString(R.string.password), "Password123")

        // Check password visibility toggle button again and perform click
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_shown))
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        // Check if content description changed
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()

        // Clear email field input
        composeTestRule.onNodeWithText(res.getString(R.string.email)).performTextClearance()

        // When email field is empty or it's value is not in correct format then the password field
        // should be disabled
        composeTestRule.onNodeWithText(res.getString(R.string.password))
            .assertExists()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun signUp_signIn_button_click_test() {
        // Find and perform click on AnnotatedString node
        composeTestRule.onNode(
            hasText(
                res.getString(R.string.sign_prompt).format(res.getString(R.string.sign_up))
            )
        )
            .performTouchInput { click(percentOffset(.9f, .5f)) }

        // Check if button text changed
        composeTestRule.onNode(hasClickAction() and hasText(res.getString(R.string.sign_up)))
            .assertExists()
            .assertIsDisplayed()

        // Check if text value in AnnotatedString changed
        composeTestRule.onNode(
            hasText(
                res.getString(R.string.sign_prompt).format(res.getString(R.string.sign_in))
            )
        ).assertExists().assertIsDisplayed()

        // Check if confirm password field is displayed but not enabled
        composeTestRule.onNodeWithText(res.getString(R.string.confirm_password))
            .assertExists()
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Perform click on AnnotatedString again
        composeTestRule.onNode(
            hasText(
                res.getString(R.string.sign_prompt).format(res.getString(R.string.sign_in))
            )
        )
            .performTouchInput { click(percentOffset(.9f, .5f)) }

        // Check if text value changed again
        composeTestRule.onNode(
            hasText(
                res.getString(R.string.sign_prompt).format(res.getString(R.string.sign_up))
            )
        ).assertExists().assertIsDisplayed()

        // Check if confirm password is not displayed
        composeTestRule.onNodeWithText(res.getString(R.string.confirm_password))
            .assertDoesNotExist()

        // Check if button text changed
        composeTestRule.onNode(hasClickAction() and hasText(res.getString(R.string.sign_in)))
            .assertExists()
            .assertIsDisplayed()
    }
}