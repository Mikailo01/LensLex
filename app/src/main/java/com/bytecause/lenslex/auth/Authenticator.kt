package com.bytecause.lenslex.auth

import android.content.Context
import com.bytecause.lenslex.models.SignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

interface Authenticator {

    val getFirebaseAuth: FirebaseAuth
    fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult>
    fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult>
    fun signInAnonymously(): Flow<SignInResult>
    suspend fun getGoogleCredential(context: Context): AuthCredential
    suspend fun signInUsingGoogleCredential(context: Context): SignInResult
    fun signOut()
}