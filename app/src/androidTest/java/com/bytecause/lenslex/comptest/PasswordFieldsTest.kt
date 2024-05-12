package com.bytecause.lenslex.comptest

import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.PasswordFields
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordFieldsTest {

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @get:Rule
    val composeTestRule = createComposeRule()

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
        }
        Assert.assertEquals(password == "Password123", true)

        confirmPasswordFieldNode.apply {
            assertIsDisplayed()
            assertIsEnabled()
            performTextInput("Password123")
        }
        Assert.assertEquals(confirmPassword == "Password123", true)

        confirmPasswordFieldNode.apply {
            performTextClearance()
            assertIsDisplayed()
            assertIsEnabled()
            performTextInput("Password12")
        }
        Assert.assertEquals(confirmPassword == "Password12", true)

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("semanticsNodes")
    }
}