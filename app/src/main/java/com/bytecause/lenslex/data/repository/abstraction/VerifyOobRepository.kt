package com.bytecause.lenslex.data.repository.abstraction

import com.bytecause.lenslex.util.ApiResult

interface VerifyOobRepository {
    suspend fun verifyOob(oobCode: String): ApiResult<Boolean>
}