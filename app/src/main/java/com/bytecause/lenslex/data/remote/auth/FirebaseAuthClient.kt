package com.bytecause.lenslex.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.bytecause.lenslex.BuildConfig
import com.bytecause.lenslex.data.remote.auth.abstraction.Authenticator
import com.bytecause.lenslex.domain.models.UserData
import com.bytecause.lenslex.ui.models.SignInResult
import com.bytecause.lenslex.util.Util
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.cancellation.CancellationException

class FirebaseAuthClient : Authenticator {
    override fun signInUsingGoogleCredential(context: Context): Flow<SignInResult> = callbackFlow {
        try {
            val googleCredential = getGoogleCredential(context)

            getAuth().signInWithCredential(googleCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(SignInResult(data = getAuth().currentUser?.run {
                            UserData(
                                userId = uid,
                                userName = displayName,
                                profilePictureUrl = photoUrl.toString(),
                                isAnonymous = isAnonymous
                            )
                        }, errorMessage = null))
                    } else {
                        trySend(
                            SignInResult(
                                data = null,
                                errorMessage = task.exception?.message
                            )
                        )
                    }
                }

        } catch (e: Exception) {
            trySend(
                SignInResult(data = null, errorMessage = e.message)
            )
        }
        awaitClose()
    }

    override fun signInAnonymously(): Flow<SignInResult> = callbackFlow {
        getAuth().signInAnonymously().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                trySend(
                    SignInResult(
                        data = task.result.user?.run {
                            UserData(
                                userId = uid,
                                userName = displayName,
                                profilePictureUrl = photoUrl.toString(),
                                isAnonymous = isAnonymous
                            )
                        },
                        errorMessage = null
                    )
                )
            } else {
                trySend(
                    SignInResult(
                        data = null,
                        errorMessage = task.exception?.message
                    )
                )
            }
        }
        awaitClose()
    }


    override fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        trySend(
                            SignInResult(
                                data = task.result.user?.run {
                                    UserData(
                                        userId = uid,
                                        userName = displayName,
                                        profilePictureUrl = photoUrl.toString(),
                                        isAnonymous = isAnonymous
                                    )
                                },
                                errorMessage = null
                            )
                        )
                    } else {
                        trySend(
                            SignInResult(
                                data = null,
                                errorMessage = task.exception?.message
                            )
                        )
                    }
                }
            awaitClose()
        }

    override fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        trySend(
                            SignInResult(
                                data = task.result.user?.run {
                                    UserData(
                                        userId = uid,
                                        userName = displayName,
                                        profilePictureUrl = photoUrl.toString(),
                                        isAnonymous = isAnonymous
                                    )
                                },
                                errorMessage = null
                            )
                        )
                    } else {
                        trySend(
                            SignInResult(
                                data = null,
                                errorMessage = task.exception?.message
                            )
                        )
                    }
                }
            awaitClose()
        }

    override fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override fun reauthenticateWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<Result<Unit>> = callbackFlow {
        val credential = EmailAuthProvider.getCredential(email, password)

        getAuth().currentUser?.reauthenticate(credential)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    // Those methods haven't be overriden, because it takes or returns object from Firebase library
    // and interface abstractions must be independent of specific libraries.

    suspend fun getGoogleCredential(context: Context): AuthCredential {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .setNonce(Util.generateNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val googleIdToken = googleIdTokenCredential.idToken

        return GoogleAuthProvider.getCredential(googleIdToken, null)
    }

    fun getAuth(): FirebaseAuth = Firebase.auth

    fun reauthenticateWithGoogle(authCredential: AuthCredential): Flow<Result<Unit>> =
        callbackFlow {
            getAuth().currentUser?.reauthenticateAndRetrieveData(authCredential)
                ?.addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                ?.addOnFailureListener { exception ->
                    trySend(Result.failure(exception))
                }
            awaitClose()
        }

    fun linkGoogleProvider(authCredential: AuthCredential): Flow<Result<Unit>> = callbackFlow {
        getAuth().currentUser?.linkWithCredential(authCredential)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }
}
