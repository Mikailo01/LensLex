package com.bytecause.lenslex.comptest

import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.PasswordFields
import com.bytecause.lenslex.util.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordFieldsTest {

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @get:Rule
    val composeTestRule = createComposeRule()

    /*@Before
    fun setUp() {
        composeTestRule.setContent {
            var password by mutableStateOf("")
            var confirmPassword by mutableStateOf("")
            var isPasswordVisible by mutableStateOf(false)

            PasswordFields(
                password = password,
                confirmPassword = confirmPassword,
                isPasswordEnabled = true,
                isPasswordVisible = isPasswordVisible,
                passwordErrors = emptyList(),
                onPasswordValueChange = { password = it },
                onConfirmPasswordValueChange = { confirmPassword = it },
                onPasswordVisibilityClick = { isPasswordVisible = it },
                onCredentialChanged = {}
            )
        }
    }*/

    @Test
    fun passwordFieldsTest() {
        var password by mutableStateOf("")
        var confirmPassword by mutableStateOf("")
        var isPasswordVisible by mutableStateOf(false)

        composeTestRule.setContent {
            PasswordFields(
                password = password,
                confirmPassword = confirmPassword,
                isPasswordEnabled = true,
                isPasswordVisible = isPasswordVisible,
                passwordErrors = emptyList(),
                onPasswordValueChange = { password = it },
                onConfirmPasswordValueChange = { confirmPassword = it },
                onPasswordVisibilityClick = { isPasswordVisible = it },
                onCredentialChanged = {}
            )
        }

        val passwordFieldNode = composeTestRule.onNodeWithText(res.getString(R.string.password))
        val confirmPasswordFieldNode =
            composeTestRule.onNodeWithText(res.getString(R.string.confirm_password))
        val passwordRulesNode =
            composeTestRule.onNodeWithTag(TestTags.PASSWORD_RULES)

        passwordRulesNode.assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .performClick()

        passwordFieldNode.apply {
            assertIsDisplayed()
            assertIsEnabled()
            performTextClearance()
            assertTextContains("")
        }

        confirmPasswordFieldNode
            .assertIsDisplayed()
            .assertIsNotEnabled()

        passwordFieldNode.apply {
            assertIsDisplayed()
            assertIsEnabled()
            performTextInput("Password123")
            assertTextEquals(res.getString(R.string.password), "Password123")
        }

        confirmPasswordFieldNode.apply {
            assertIsDisplayed()
            assertIsEnabled()
            performTextInput("Password123")
            assertTextEquals(res.getString(R.string.confirm_password), "Password123")
            performTextClearance()
            assertIsDisplayed()
            assertIsEnabled()
            performTextInput("Password12")
        }

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_shown))
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .assertExists().assertIsDisplayed().assertIsEnabled()

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("semanticsNodes")
    }
}