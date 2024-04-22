package com.bytecause.lenslex.util

import com.bytecause.lenslex.models.Credentials
import java.security.MessageDigest
import java.util.UUID

object ValidationUtil {

    fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun emailValidator(email: String): Boolean {
        val regex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")
        return regex.matches(email)
    }

    fun areCredentialsValid(credentials: Credentials.Sensitive): CredentialValidationResult {
        return when (credentials) {
            is Credentials.Sensitive.SignInCredentials -> {

                val isEmailValid: Boolean = emailValidator(credentials.email)
                val isPasswordValid = passwordValidator(password = credentials.password)

                if (isEmailValid && isPasswordValid is PasswordValidationResult.Valid) CredentialValidationResult.Valid
                else CredentialValidationResult.Invalid(
                    isEmailValid = isEmailValid,
                    passwordError = isPasswordValid as? PasswordValidationResult.Invalid
                )
            }

            is Credentials.Sensitive.SignUpCredentials -> {

                val isEmailValid: Boolean = emailValidator(credentials.email)
                val passwordValidationResult =
                    passwordValidator(credentials.password, credentials.confirmPassword)

                return when {
                    isEmailValid && passwordValidationResult is PasswordValidationResult.Valid -> {
                        CredentialValidationResult.Valid
                    }

                    isEmailValid && passwordValidationResult is PasswordValidationResult.Invalid -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = true,
                            passwordError = passwordValidationResult
                        )
                    }

                    !isEmailValid && passwordValidationResult is PasswordValidationResult.Valid -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = isEmailValid,
                            passwordError = null
                        )
                    }

                    else -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = isEmailValid,
                            passwordError = passwordValidationResult
                        )
                    }

                }
            }

            is Credentials.Sensitive.PasswordCredential -> {
                val passwordValidationResult =
                    passwordValidator(credentials.password, credentials.confirmPassword)


                return when (passwordValidationResult) {
                    is PasswordValidationResult.Valid -> {
                        CredentialValidationResult.Valid
                    }

                    else -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = true,
                            passwordError = passwordValidationResult
                        )
                    }
                }
            }

            is Credentials.Sensitive.EmailCredential -> {
                return when (emailValidator(credentials.email)) {
                    true -> {
                        CredentialValidationResult.Valid
                    }

                    else -> {
                        CredentialValidationResult.Invalid(
                            isEmailValid = false,
                            passwordError = null
                        )
                    }
                }
            }
        }
    }
}

private fun passwordValidator(
    password: String,
    confirmPassword: String? = null
): PasswordValidationResult {
    val errors = mutableListOf<PasswordErrorType>()

    val isPasswordEmpty: Boolean = password.isBlank()

    val passwordMatch: Boolean? =
        if (confirmPassword != null) password == confirmPassword else null
    val passwordLengthInRange: Boolean = password.length in 8..24
    val upperCase: Boolean = password.any { it.isUpperCase() }
    val lowerCase: Boolean = password.any { it.isLowerCase() }
    val containsNumber: Boolean = password.any { it.isDigit() }

    if (isPasswordEmpty) errors.add(PasswordErrorType.PASSWORD_EMPTY)
    if (passwordMatch == false) errors.add(PasswordErrorType.PASSWORD_MISMATCH)
    if (!passwordLengthInRange) errors.add(PasswordErrorType.LENGTH_OUT_OF_BOUNDS)
    if (!upperCase) errors.add(PasswordErrorType.MISSING_UPPER_CASE)
    if (!lowerCase) errors.add(PasswordErrorType.MISSING_LOWER_CASE)
    if (!containsNumber) errors.add(PasswordErrorType.MISSING_DIGIT)

    return if (errors.isEmpty()) {
        PasswordValidationResult.Valid
    } else {
        if (errors.all { it != PasswordErrorType.PASSWORD_MISMATCH }) errors.add(PasswordErrorType.PASSWORD_INCORRECT)
        PasswordValidationResult.Invalid(errors)
    }
}

sealed interface CredentialValidationResult {
    data object Valid : CredentialValidationResult
    data class Invalid(val isEmailValid: Boolean?, val passwordError: PasswordValidationResult?) :
        CredentialValidationResult
}

sealed interface PasswordValidationResult {
    data object Valid : PasswordValidationResult
    data class Invalid(val cause: List<PasswordErrorType>) : PasswordValidationResult
}

enum class PasswordErrorType {
    PASSWORD_INCORRECT,
    PASSWORD_EMPTY,
    PASSWORD_MISMATCH,
    LENGTH_OUT_OF_BOUNDS,
    MISSING_LOWER_CASE,
    MISSING_UPPER_CASE,
    MISSING_DIGIT
}