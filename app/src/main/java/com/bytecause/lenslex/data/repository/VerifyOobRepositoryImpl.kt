package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.remote.retrofit.VerifyOobCodeRestApiService
import com.bytecause.lenslex.data.repository.abstraction.VerifyOobRepository
import com.bytecause.lenslex.util.ApiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VerifyOobRepositoryImpl(
    private val service: VerifyOobCodeRestApiService,
    private val coroutineDispatcher: CoroutineDispatcher,
): VerifyOobRepository {

    // If status code == 200 then password reset code is still valid.
    override suspend fun verifyOob(oobCode: String): ApiResult<Boolean> {
        return withContext(coroutineDispatcher) {
            try {
                val result = service.verify(oobCode = oobCode)
                ApiResult.Success(data = result.code() == 200)
            } catch (e: Exception) {
                e.printStackTrace()
                ApiResult.Failure(exception = e)
            }
        }
    }
}