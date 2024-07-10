package com.bytecause.lenslex.data.repository

import android.net.Uri
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.domain.models.UserAccountDetails
import com.bytecause.lenslex.ui.interfaces.Provider
import com.bytecause.lenslex.util.BaseCollection
import com.bytecause.lenslex.util.WordsCollection
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull

private const val FirebaseProviderId = "firebase"

class UserRepositoryImpl(
    private val auth: FirebaseAuthClient,
    private val firestore: FirebaseFirestore,
) : UserRepository {

    private fun user(): FirebaseUser? = auth.getAuth().currentUser

    override fun getUserData(): UserAccountDetails? =
        user()?.run {
            UserAccountDetails(
                uid = uid,
                creationTimeStamp = metadata?.creationTimestamp,
                userName = displayName?.takeIf { it.isNotBlank() } ?: "",
                email = providerData.find { it.providerId == FirebaseProviderId }?.email.takeIf { it?.isNotBlank() == true }
                    ?: providerData.find { it.email?.isNotBlank() == true }?.email,
                profilePictureUrl = photoUrl.toString(),
                isAnonymous = isAnonymous
            )
        }

    override fun linkedProviders(): List<Provider> =
        user()?.run {
            val providers = mutableListOf<Provider>()

            reload()

            // Check for email
            if (providerData.find { it.providerId == EmailAuthProvider.PROVIDER_ID } != null) {
                providers.add(Provider.Email)
            }

            // Check for Google provider directly
            if (providerData.find { it.providerId == GoogleAuthProvider.PROVIDER_ID } != null) {
                providers.add(Provider.Google)
            }

            providers.toList()
        } ?: emptyList()

    private fun deleteUserProfilePicture(userId: String): Flow<Boolean> = callbackFlow {
        Firebase
            .storage
            .reference
            .child("profile_pictures/${userId}")
            .delete()
            .addOnCompleteListener {
                trySend(it.isSuccessful)
            }
        awaitClose()
    }

    private fun deleteSavedExpressions(userId: String): Flow<Boolean> = callbackFlow {
        firestore
            .collection(BaseCollection)
            .document(userId)
            .collection(WordsCollection)
            .get()
            .addOnSuccessListener { collection ->
                val batch = FirebaseFirestore.getInstance().batch()
                for (doc in collection.documents) {
                    batch.delete(doc.reference)
                }
                // Deletes all documents in single transaction (single server call)
                batch.commit()
                    .addOnSuccessListener {
                        firestore.collection(BaseCollection)
                            .document(userId)
                            .delete().addOnSuccessListener {
                                trySend(true)
                            }
                    }
            }
        awaitClose()
    }

    override fun deleteUserData(): Flow<Boolean> = callbackFlow {
        user()?.uid?.let { userId ->
            val profilePictureDeletionTask = deleteUserProfilePicture(userId).firstOrNull() ?: false
            val savedExpressionsDeletionTask = deleteSavedExpressions(userId).firstOrNull() ?: false
            trySend(profilePictureDeletionTask && savedExpressionsDeletionTask)
        }
        awaitClose()
    }

    override fun deleteUserAccount(): Flow<Result<Unit>> = callbackFlow {
        deleteUserData().firstOrNull()?.let {
            user()?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success(Unit))
                } else {
                    task.exception?.let { exception ->
                        trySend(Result.failure(exception))
                    }
                }
            }
        }
        awaitClose()
    }

    override fun updateProfilePicture(uri: Uri) {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        user()?.updateProfile(changeRequest)
    }

    override fun updateUsername(username: String): Flow<Result<Unit>> = callbackFlow {
        val changeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user()?.updateProfile(changeRequest)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    override fun reloadUserData(): UserAccountDetails? {
        user()?.reload()

        return getUserData()
    }

    override fun linkEmailProvider(email: String, password: String): Flow<Result<Unit>> =
        callbackFlow {
            val emailCredentials =
                EmailAuthProvider.getCredential(email, password)
            user()?.linkWithCredential(emailCredentials)
                ?.addOnSuccessListener {
                    trySend(Result.success(Unit))
                }
                ?.addOnFailureListener { exception ->
                    trySend(Result.failure(exception))
                }
            awaitClose()
        }

    override fun unlinkGoogleProvider(): Flow<Result<Unit>> = callbackFlow {
        user()?.unlink(GoogleAuthProvider.PROVIDER_ID)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    override fun unlinkEmailProvider(): Flow<Result<Unit>> = callbackFlow {
        user()?.unlink(EmailAuthProvider.PROVIDER_ID)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    override fun updateEmail(email: String): Flow<Result<Unit>> = callbackFlow {
        user()?.verifyBeforeUpdateEmail(email)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }

    override fun updatePassword(password: String): Flow<Result<Unit>> = callbackFlow {
        user()?.updatePassword(password)
            ?.addOnSuccessListener {
                trySend(Result.success(Unit))
            }
            ?.addOnFailureListener { exception ->
                trySend(Result.failure(exception))
            }
        awaitClose()
    }
}