package com.bytecause.lenslex.ui.screens.viewmodel

import android.app.Application

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.ui.interfaces.Credentials
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.SignInState
import com.bytecause.lenslex.models.uistate.LoginState
import com.bytecause.lenslex.ui.events.LoginUiEvent
import com.bytecause.lenslex.util.CredentialValidationResult
import com.bytecause.lenslex.util.ValidationUtil.areCredentialsValid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val auth: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    /* private val credentialManager by lazy {
         CredentialManager.create(application)
     }*/

    private val _signUiState = MutableStateFlow(SignInState())
    val signUiState = _signUiState.asStateFlow()

    fun uiEventHandler(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnCredentialChanged -> {
                _uiState.update {
                    it.copy(
                        credentialValidationResult = areCredentialsValid(event.value)
                    )
                }
            }

            is LoginUiEvent.OnCredentialsEntered -> {
                areCredentialsValid(
                    if (_uiState.value.signIn) Credentials.Sensitive.SignInCredentials(
                        _uiState.value.email,
                        _uiState.value.password
                    ) else Credentials.Sensitive.SignUpCredentials(
                        email = _uiState.value.email,
                        password = _uiState.value.password,
                        confirmPassword = _uiState.value.confirmPassword
                    )
                ).let { validationResult ->

                    _uiState.update {
                        it.copy(credentialValidationResult = validationResult)
                    }

                    if (validationResult is CredentialValidationResult.Valid) {
                        viewModelScope.launch {
                            when (_uiState.value.signIn) {
                                true -> {
                                    _uiState.update {
                                        it.copy(isLoading = true)
                                    }

                                    signInViaEmailAndPasswordIfValid(
                                        Credentials.Sensitive.SignInCredentials(
                                            email = _uiState.value.email,
                                            password = _uiState.value.password
                                        )
                                    )
                                }

                                false -> {
                                    _uiState.update {
                                        it.copy(isLoading = true)
                                    }

                                    signUpViaEmailAndPassword(
                                        Credentials.Sensitive.SignUpCredentials(
                                            email = _uiState.value.email,
                                            password = _uiState.value.password,
                                            confirmPassword = _uiState.value.confirmPassword
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is LoginUiEvent.OnEmailValueChange -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = event.value,
                                    password = _uiState.value.password
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = event.value,
                                    password = _uiState.value.password,
                                    confirmPassword = _uiState.value.confirmPassword
                                )
                            }
                        )
                    )
                }
            }

            is LoginUiEvent.OnPasswordValueChange -> {
                _uiState.update {
                    it.copy(
                        password = event.value,
                        credentialValidationResult = areCredentialsValid(
                            if (_uiState.value.signIn) {
                                Credentials.Sensitive.SignInCredentials(
                                    email = _uiState.value.email,
                                    password = event.value
                                )
                            } else {
                                Credentials.Sensitive.SignUpCredentials(
                                    email = _uiState.value.email,
                                    password = event.value,
                                    confirmPassword = _uiState.value.confirmPassword
                                )
                            }
                        )
                    )
                }
            }

            is LoginUiEvent.OnConfirmPasswordChange -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.value,
                        credentialValidationResult = areCredentialsValid(
                            Credentials.Sensitive.SignUpCredentials(
                                email = _uiState.value.email,
                                password = _uiState.value.password,
                                confirmPassword = event.value
                            )
                        )
                    )
                }
            }

            LoginUiEvent.OnPasswordsVisibilityChange -> {
                _uiState.update {
                    it.copy(passwordVisible = !it.passwordVisible)
                }
            }

            LoginUiEvent.OnAnnotatedStringClick -> {
                _uiState.update {
                    it.copy(
                        email = "",
                        password = "",
                        confirmPassword = "",
                        credentialValidationResult = null,
                        signIn = !it.signIn
                    )
                }
            }

            LoginUiEvent.OnSignInAnonymously -> {
                viewModelScope.launch {
                    signInAnonymously()
                }
            }

            else -> {
                // do nothing
            }
        }
    }

    fun onSignInResult(result: SignInResult) {
        _signUiState.update {
            _signUiState.value.copy(
                isSignInSuccessful = (result.data != null),
                signInError = result.errorMessage
            )
        }
    }

    fun signInUsingGoogleCredential(context: Context) {
        viewModelScope.launch {
            onSignInResult(auth.signInUsingGoogleCredential(context))
        }
    }

    /*private suspend fun saveCredential(context: Context, username: String, password: String) {
        try {
            // Ask the user for permission to add the credentials to their store
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(username, password)
            )
            Log.v("CredentialTest", "Credentials successfully added")
        } catch (e: CreateCredentialCancellationException) {
            // do nothing, the user chose not to save the credential
            Log.v("CredentialTest", "User cancelled the save")
        } catch (e: CreateCredentialException) {
            Log.v("CredentialTest", "Credential save error", e)
        }
    }

    private suspend fun getCredential(context: Context): PasswordCredential? {
        try {
            // GetPasswordOption() tell the credential library that we're only interested in password credentials
            // Show the user a dialog allowing them to pick a saved credential
            val credentialResponse = credentialManager.getCredential(
                request = GetCredentialRequest(
                    listOf(GetPasswordOption())
                ),
                context = context
            )

            // Return the selected credential (as long as it's a username/password)
            return credentialResponse.credential as? PasswordCredential
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the request. Return nothing
            return null
        } catch (e: NoCredentialException) {
            // We don't have a matching credential
            return null
        } catch (e: GetCredentialException) {
            Log.e("CredentialTest", "Error getting credential", e)
            throw e
        }
    }*/

    /* fun signInWithSavedCredential(context: Context) {
         viewModelScope.launch {
             try {
                 val passwordCredential = getCredential(context) ?: return@launch

                 signInUsingEmailAndPassword(
                     Credentials.Sensitive.SignInCredentials(
                         email = passwordCredential.id,
                         password = passwordCredential.password
                     )
                 ).firstOrNull()?.let {
                     onSignInResult(it)
                 }
             } catch (e: Exception) {
                 Log.e("CredentialTest", "Error getting credential", e)
             }
         }
     } */

    fun signInUsingEmailAndPassword(
        credentials: Credentials.Sensitive.SignInCredentials
    ): Flow<SignInResult> {
        return auth.signInViaEmailAndPassword(credentials.email, credentials.password)
    }

    suspend fun signInViaEmailAndPasswordIfValid(
        credentials: Credentials.Sensitive.SignInCredentials
    ) {
        signInUsingEmailAndPassword(credentials).firstOrNull()?.let {
            /*if (it.data != null) saveCredential(
                context = context,
                credentials.email,
                credentials.password
            )*/
            onSignInResult(it)
        }
    }

    suspend fun signUpViaEmailAndPassword(
        credentials: Credentials.Sensitive.SignUpCredentials
    ) {
        auth.signUpViaEmailAndPassword(credentials.email, credentials.password)
            .firstOrNull()?.let {
                onSignInResult(it)
                /* if (it.data != null) saveCredential(
                     context = context,
                     credentials.email,
                     credentials.password
                 )*/
            }
    }

    private suspend fun signInAnonymously() {
        auth.signInAnonymously().firstOrNull()?.let {
            onSignInResult(it)
        }
    }
}