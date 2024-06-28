package com.bytecause.lenslex.data.repository

import android.content.Context
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.domain.models.SignInResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

class FakeAuthenticatorImpl : Authenticator {
    override fun getAuth(): FirebaseAuth {
        return Firebase.auth
    }

    override fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override fun signInAnonymously(): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override suspend fun getGoogleCredential(context: Context): AuthCredential {
        TODO("Not yet implemented")
    }

    override suspend fun signInUsingGoogleCredential(context: Context): SignInResult {
        TODO("Not yet implemented")
    }

    override fun signOut() {
        TODO("Not yet implemented")
    }
}