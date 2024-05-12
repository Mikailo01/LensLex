package com.bytecause.lenslex.comptest

import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bytecause.lenslex.R
import com.bytecause.lenslex.ui.components.EmailField
import com.bytecause.lenslex.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmailFieldTest {

    private val res: Resources =
        InstrumentationRegistry.getInstrumentation().targetContext.resources

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emailFieldTest() {
        val emailValue = "noreply@gmail.com"

        composeTestRule.setContent {
            AppTheme {
                EmailField(
                    emailValue = emailValue,
                    isEmailError = false,
                    onEmailValueChanged = {}
                )
            }
        }

        composeTestRule.onNodeWithText(emailValue).assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText(res.getString(R.string.email_unsupported_format_warning))
            .assertDoesNotExist()
    }
}