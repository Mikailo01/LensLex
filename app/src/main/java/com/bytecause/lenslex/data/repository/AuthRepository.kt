package com.bytecause.lenslex.data.repository

import android.content.Context
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.models.SignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class AuthRepository(
    private val auth: Authenticator,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    val getFirebaseAuth: FirebaseAuth = auth.getFirebaseAuth

    fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        auth.signUpViaEmailAndPassword(email, password)
            .flowOn(coroutineDispatcher)

    fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> =
        auth.signInViaEmailAndPassword(email, password)
            .flowOn(coroutineDispatcher)


    fun signInAnonymously(): Flow<SignInResult> =
        auth.signInAnonymously().flowOn(coroutineDispatcher)

    suspend fun signInUsingGoogleCredential(context: Context) =
        withContext(coroutineDispatcher) {
            auth.signInUsingGoogleCredential(context)
        }

    fun signOut() = auth.signOut()
}