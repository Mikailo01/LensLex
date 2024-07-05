package com.bytecause.lenslex.data.remote.auth

import android.content.Context
import com.bytecause.lenslex.ui.models.SignInResult
import kotlinx.coroutines.flow.Flow

interface Authenticator {
    fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult>
    fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult>
    fun signInAnonymously(): Flow<SignInResult>
    fun signInUsingGoogleCredential(context: Context): Flow<SignInResult>
    fun signOut()
    fun reauthenticateWithEmailAndPassword(email: String, password: String): Flow<Result<Unit>>
}