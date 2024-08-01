package com.bytecause.lenslex.data

import android.content.Context
import androidx.credentials.PasswordCredential
import com.bytecause.lenslex.data.remote.auth.abstraction.CredentialManager

class FakeCredentialManagerImpl: CredentialManager {
    override suspend fun getCredential(context: Context): PasswordCredential? = null

    override suspend fun saveCredential(context: Context, username: String, password: String) {
        TODO("Not yet implemented")
    }
}