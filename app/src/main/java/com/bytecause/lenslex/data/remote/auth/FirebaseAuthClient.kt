package com.bytecause.lenslex.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.bytecause.lenslex.R
import com.bytecause.lenslex.models.SignInResult
import com.bytecause.lenslex.models.UserData
import com.bytecause.lenslex.util.ValidationUtil
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthClient : Authenticator {

    override val getAuth: FirebaseAuth = Firebase.auth

    override suspend fun getGoogleCredential(context: Context): AuthCredential {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setNonce(ValidationUtil.generateNonce())
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

    override suspend fun signInUsingGoogleCredential(context: Context): SignInResult {
        return try {
            val googleCredential = getGoogleCredential(context)

            suspendCoroutine {
                getAuth.signInWithCredential(googleCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            it.resume(SignInResult(data = getAuth.currentUser?.run {
                                UserData(
                                    userId = uid,
                                    userName = displayName,
                                    profilePictureUrl = photoUrl.toString(),
                                    isAnonymous = isAnonymous
                                )
                            }, errorMessage = null))
                        } else {
                            it.resume(
                                SignInResult(
                                    data = null,
                                    errorMessage = task.exception?.message
                                )
                            )
                        }
                    }
            }

        } catch (e: Exception) {
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    override fun signInAnonymously(): Flow<SignInResult> = callbackFlow {
        getAuth.signInAnonymously().addOnCompleteListener { task ->

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

    override fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getAuth.createUserWithEmailAndPassword(email, password)
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

    override fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        callbackFlow {
            getAuth.signInWithEmailAndPassword(email, password)
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

    override fun signOut() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }
}