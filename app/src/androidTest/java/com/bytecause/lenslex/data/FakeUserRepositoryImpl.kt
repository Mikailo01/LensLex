package com.bytecause.lenslex.data

import android.net.Uri
import com.bytecause.lenslex.data.repository.abstraction.UserRepository
import com.bytecause.lenslex.domain.models.UserAccountDetails
import com.bytecause.lenslex.ui.interfaces.Provider
import kotlinx.coroutines.flow.Flow

class FakeUserRepositoryImpl: UserRepository {
    override fun getUserData(): UserAccountDetails = UserAccountDetails(
        // Dummy values
        uid = "laudem",
        creationTimeStamp = null,
        userName = null,
        email = null,
        profilePictureUrl = "https://www.google.com/#q=moderatius",
        isAnonymous = false
    )

    override fun linkedProviders(): List<Provider> {
        TODO("Not yet implemented")
    }

    override fun deleteUserData(): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteUserAccount(): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateProfilePicture(uri: Uri) {
        TODO("Not yet implemented")
    }

    override fun updatePassword(password: String): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateEmail(email: String): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateUsername(username: String): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun reloadUserData(): UserAccountDetails = UserAccountDetails(
        uid = "laudem",
        creationTimeStamp = null,
        userName = null,
        email = null,
        profilePictureUrl = "https://www.google.com/#q=moderatius",
        isAnonymous = false
    )

    override fun linkEmailProvider(email: String, password: String): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun unlinkGoogleProvider(): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }

    override fun unlinkEmailProvider(): Flow<Result<Unit>> {
        TODO("Not yet implemented")
    }
}