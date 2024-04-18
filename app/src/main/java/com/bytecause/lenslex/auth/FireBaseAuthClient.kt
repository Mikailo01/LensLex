package com.bytecause.lenslex.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FireBaseAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    val getFirebaseAuth: FirebaseAuth = Firebase.auth

    suspend fun signInViaGoogle(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildGoogleSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    private fun buildGoogleSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    suspend fun signInWithGoogleIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = getFirebaseAuth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        userName = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    fun getGoogleCredentials(intent: Intent): AuthCredential {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        return GoogleAuthProvider.getCredential(googleIdToken, null)
    }

    fun signInAnonymously(): Flow<SignInResult> = callbackFlow {
        getFirebaseAuth.signInAnonymously().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                trySend(
                    SignInResult(
                        data = task.result.user?.let { user ->
                            UserData(
                                userId = user.uid,
                                userName = user.displayName,
                                profilePictureUrl = user.photoUrl.toString()
                            )
                        },
                        errorMessage = null
                    )
                ).isSuccess
            } else {
                trySend(
                    SignInResult(
                        data = null,
                        errorMessage = task.exception?.message
                    )
                ).isSuccess
            }
        }

        awaitClose { cancel() }
    }

    fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        trySend(
                            SignInResult(
                                data = task.result.user?.let { user ->
                                    UserData(
                                        userId = user.uid,
                                        userName = user.displayName,
                                        profilePictureUrl = user.photoUrl.toString()
                                    )
                                },
                                errorMessage = null
                            )
                        ).isSuccess
                    } else {
                        trySend(
                            SignInResult(
                                data = null,
                                errorMessage = task.exception?.message
                            )
                        ).isSuccess
                    }
                }

            awaitClose { cancel() }
        }

    fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        trySend(
                            SignInResult(
                                data = task.result.user?.let { user ->
                                    UserData(
                                        userId = user.uid,
                                        userName = user.displayName,
                                        profilePictureUrl = user.photoUrl.toString()
                                    )
                                },
                                errorMessage = null
                            )
                        ).isSuccess
                    } else {
                        trySend(
                            SignInResult(
                                data = null,
                                errorMessage = task.exception?.message.also {
                                    Log.d(
                                        "auth",
                                        it.toString()
                                    )
                                }
                            )
                        ).isSuccess
                    }
                }

            awaitClose { cancel() }
        }

    fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }
}