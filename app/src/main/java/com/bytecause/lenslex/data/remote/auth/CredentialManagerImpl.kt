package com.bytecause.lenslex.data.remote.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.bytecause.lenslex.data.remote.auth.abstraction.CredentialManager

class CredentialManagerImpl : CredentialManager {

    override suspend fun getCredential(
        context: Context
    ): PasswordCredential? {
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            // GetPasswordOption() tell the credential library that we're only interested in password credentials
            // Show the user a dialog allowing them to pick a saved credential
            val credentialResponse = credentialManager.getCredential(
                request = GetCredentialRequest(
                    listOf(GetPasswordOption())
                ),
                context = context
            )

            // Return the selected credential (as long as it's a username/password)
            return credentialResponse.credential as? PasswordCredential
        } catch (e: GetCredentialCancellationException) {
            // User cancelled the request. Return nothing
            return null
        } catch (e: NoCredentialException) {
            // We don't have a matching credential
            return null
        } catch (e: GetCredentialException) {
            Log.e("CredentialTest", "Error getting credential", e)
            throw e
        }
    }

    override suspend fun saveCredential(
        context: Context,
        username: String,
        password: String
    ) {
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            // Ask the user for permission to add the credentials to their store
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(username, password)
            )
        } catch (e: CreateCredentialCancellationException) {
            // do nothing, the user chose not to save the credential
        } catch (e: CreateCredentialException) {
            Log.v("CredentialTest", "Credential save error", e)
        }
    }
}