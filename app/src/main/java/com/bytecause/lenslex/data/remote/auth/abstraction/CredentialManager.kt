package com.bytecause.lenslex.data.remote.auth.abstraction

import android.content.Context
import androidx.credentials.PasswordCredential

interface CredentialManager {
    suspend fun getCredential(
        context: Context
    ): PasswordCredential?

    suspend fun saveCredential(
        context: Context,
        username: String,
        password: String
    )
}