package com.bytecause.lenslex

import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.PasswordErrorType
import com.bytecause.lenslex.util.PasswordValidationResult
import com.bytecause.lenslex.util.ValidationUtil
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidationTests {

    @Test
    fun emailValidatorTest() {

        val invalidEmails = listOf(
            Credentials.Sensitive.EmailCredential("john.doe@example.abcde"),
            Credentials.Sensitive.EmailCredential("john.doe!@example.com"),
            Credentials.Sensitive.EmailCredential("john.doe@example.com!"),
            Credentials.Sensitive.EmailCredential("@example.com"),
            Credentials.Sensitive.EmailCredential("johndoeexample.com"),
            Credentials.Sensitive.EmailCredential("john.doe@example")
        )
        val validEmails = listOf(
            Credentials.Sensitive.EmailCredential("john.doe.middle@example.com"),
            Credentials.Sensitive.EmailCredential("john.doe@sub-domain.example.com"),
            Credentials.Sensitive.EmailCredential("john.doe@example123.com"),
            Credentials.Sensitive.EmailCredential("john.doe@example.co.uk"),
            Credentials.Sensitive.EmailCredential("john.doe@sub.example.com"),
            Credentials.Sensitive.EmailCredential("john.doe@example.com")
        )

        for (email in invalidEmails) {
            assertEquals(
                ValidationUtil.areCredentialsValid(email),
                CredentialValidationResult.Invalid(isEmailValid = false, passwordError = null)
            )
        }

        for (email in validEmails) {
            assertEquals(ValidationUtil.areCredentialsValid(email), CredentialValidationResult.Valid)
        }
    }

    @Test
    fun passwordValidatorTest() {
        val invalidPasswords = listOf(
            "PASSWORD123",
            "P@w0",
            "ThisIsALongPasswordThatExceedsTheMaximumLength123",
            "",
            "Pass",
            "password123",
            "Password"
        )
        val validPasswords = listOf(
            "Password123",
            "P@ssw0rd",
            "ThisIsLongPassword123456",
            "paSSwor3"
        )

        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[0]),
            arrayOf(PasswordErrorType.MISSING_LOWER_CASE, PasswordErrorType.PASSWORD_INCORRECT)
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[1]),
            arrayOf(PasswordErrorType.LENGTH_OUT_OF_BOUNDS, PasswordErrorType.PASSWORD_INCORRECT)
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[2]),
            arrayOf(PasswordErrorType.LENGTH_OUT_OF_BOUNDS, PasswordErrorType.PASSWORD_INCORRECT)
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[3]),
            arrayOf(PasswordErrorType.PASSWORD_EMPTY, PasswordErrorType.PASSWORD_INCORRECT)
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[4]),
            arrayOf(
                PasswordErrorType.LENGTH_OUT_OF_BOUNDS,
                PasswordErrorType.MISSING_DIGIT,
                PasswordErrorType.PASSWORD_INCORRECT
            )
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[5]),
            arrayOf(PasswordErrorType.MISSING_UPPER_CASE, PasswordErrorType.PASSWORD_INCORRECT)
        )
        assertArrayEquals(
            invalidPasswordResult(invalidPasswords[6]),
            arrayOf(PasswordErrorType.MISSING_DIGIT, PasswordErrorType.PASSWORD_INCORRECT)
        )

        for (password in validPasswords) {
            assertEquals(validPasswordResult(password), CredentialValidationResult.Valid)
        }
    }

    private fun invalidPasswordResult(password: String): Array<PasswordErrorType> =
        ((ValidationUtil.areCredentialsValid(Credentials.Sensitive.PasswordCredential(password = password)) as CredentialValidationResult.Invalid).passwordError as PasswordValidationResult.Invalid).cause.toTypedArray()

    private fun validPasswordResult(password: String): CredentialValidationResult =
        ValidationUtil.areCredentialsValid(Credentials.Sensitive.PasswordCredential(password = password)) as CredentialValidationResult.Valid
}
