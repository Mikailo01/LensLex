package com.bytecause.lenslex.data.repository

import android.content.Context
import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.ui.models.SignInResult
import kotlinx.coroutines.flow.Flow

class FakeAuthenticatorImpl : Authenticator {

    override fun signUpViaEmailAndPassword(email: String, password: String): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override fun signInViaEmailAndPassword(email: String, password: String): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override fun signInAnonymously(): Flow<SignInResult> {
        TODO("Not yet implemented")
    }

    override fun signInUsingGoogleCredential(context: Context): Flow<SignInResult> {
        TODO("Not yet implemented")
    }


    override fun signOut() {
        TODO("Not yet implemented")
    }

    override fun reauthenticateWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }
}