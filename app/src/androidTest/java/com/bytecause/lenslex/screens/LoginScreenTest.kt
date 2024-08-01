package com.bytecause.lenslex.screens

import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.MainActivity
import com.bytecause.lenslex.R
import com.bytecause.lenslex.navigation.Screen
import com.bytecause.lenslex.navigation.navhost.popBackStackOnce
import com.bytecause.lenslex.ui.screens.LoginScreen
import com.bytecause.lenslex.ui.screens.SendEmailResetScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// TODO("Add more tests")
class LoginScreenTest {

    @get: Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Before
    fun setUp() {
        composeTestRule.activity.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Screen.Login
            ) {
                composable<Screen.Login> {
                    LoginScreen(
                        isExpandedScreen = false,
                        onNavigate = { navController.navigate(it) },
                        onUserLoggedIn = {})
                }

                composable<Screen.SendEmailPasswordReset> {
                    SendEmailResetScreen(
                        isExpandedScreen = false,
                        onNavigateBack = { navController.popBackStackOnce() })
                }
            }
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

    @Test
    fun forgot_password_click_test() {
        // Navigate to request new password screen
        composeTestRule.onNodeWithText(res.getString(R.string.forget_password))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that the text is correct
        composeTestRule.onNodeWithText(res.getString(R.string.reset_password_request))
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(res.getString(R.string.reset_password_request_message))
            .assertExists()
            .assertIsDisplayed()

        // Assert that Email field is in composition
        composeTestRule.onNodeWithText(res.getString(R.string.email))
            .assert(hasClickAction())
            .assertExists()
            .assertIsDisplayed()

        // Navigate back
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.navigate_back))
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        // Assert that we are back on Login screen
        composeTestRule.onNodeWithText(res.getString(R.string.welcome_to_lens_lex))
            .assertExists()
            .assertIsDisplayed()
    }
}