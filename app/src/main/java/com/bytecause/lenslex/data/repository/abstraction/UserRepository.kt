package com.bytecause.lenslex.data.repository.abstraction

import android.net.Uri
import com.bytecause.lenslex.domain.models.UserAccountDetails
import com.bytecause.lenslex.ui.interfaces.Provider
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserData(): UserAccountDetails?
    fun linkedProviders(): List<Provider>
    fun deleteUserData(): Flow<Boolean>
    fun deleteUserAccount(): Flow<Result<Unit>>
    fun updateProfilePicture(uri: Uri)
    fun updatePassword(password: String): Flow<Result<Unit>>
    fun updateEmail(email: String): Flow<Result<Unit>>
    fun updateUsername(username: String): Flow<Result<Unit>>
    fun reloadUserData(): UserAccountDetails?
    fun linkEmailProvider(email: String, password: String): Flow<Result<Unit>>
    fun unlinkGoogleProvider(): Flow<Result<Unit>>
    fun unlinkEmailProvider(): Flow<Result<Unit>>
}