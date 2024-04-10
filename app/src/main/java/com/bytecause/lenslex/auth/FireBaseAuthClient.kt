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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class FireBaseAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

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
            val user = auth.signInWithCredential(googleCredentials).await().user
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

    fun signInAnonymously(): Flow<SignInResult> = callbackFlow {
        auth.signInAnonymously().addOnCompleteListener { task ->

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
            auth.createUserWithEmailAndPassword(email, password)
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
            auth.signInWithEmailAndPassword(email, password)
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

    suspend fun signOut(): Boolean {
        return try {
            //oneTapClient.signOut()
            auth.signOut()
            delay(1000)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            false
        }
    }

    /*suspend fun signOut() {
         try {
             oneTapClient.signOut()
             auth.signOut()
         } catch (e: Exception) {
             e.printStackTrace()
             if (e is CancellationException) throw e
         }
     }*/

    fun getSignedInUser(): FirebaseUser? = auth.currentUser
}