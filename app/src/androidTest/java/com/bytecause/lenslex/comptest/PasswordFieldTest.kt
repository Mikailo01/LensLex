package com.bytecause.lenslex.comptest

import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.PasswordField
import com.bytecause.lenslex.util.PasswordErrorType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordFieldTest {

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun passwordFieldTest() {
        var password by mutableStateOf("")
        var passwordErrors by mutableStateOf(listOf(PasswordErrorType.PASSWORD_INCORRECT))
        var isPasswordEnabled by mutableStateOf(true)
        var isPasswordVisible by mutableStateOf(false)

        composeTestRule.setContent {
            PasswordField(
                password = password,
                passwordErrors = passwordErrors,
                isPasswordEnabled = isPasswordEnabled,
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityClick = { isPasswordVisible = !isPasswordVisible },
                onPasswordValueChange = { password = it },
                onCredentialChanged = { /* handle credential change */ }
            )
        }

        composeTestRule.onNodeWithText(res.getString(R.string.password_incorrect))
            .assertExists()
            .assertIsDisplayed()

        passwordErrors = listOf(PasswordErrorType.PASSWORD_EMPTY)

        composeTestRule.onNodeWithText(res.getString(R.string.password_empty_warning))
            .assertExists()
            .assertIsDisplayed()

        // Assert initial state
        composeTestRule.onNodeWithText("Password")
            .assertExists()
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_hidden))
            .assertIsDisplayed()
            .performClick()

        // Simulate clicking on the password visibility toggle
        composeTestRule.onNodeWithContentDescription(res.getString(R.string.password_shown))
            .assertIsDisplayed()

        // Simulate changing the password value
        password = "newPassword"
        // Assert password value change
        composeTestRule.onNodeWithText("newPassword", useUnmergedTree = true)
            .assertTextEquals("newPassword")

        // Simulate disabling the password field
        isPasswordEnabled = false
        // Assert disabled state
        composeTestRule.onNodeWithText("newPassword", useUnmergedTree = true).assertIsNotEnabled()
    }
}