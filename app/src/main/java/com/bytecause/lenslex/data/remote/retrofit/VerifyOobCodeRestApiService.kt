package com.bytecause.lenslex.data.remote.retrofit


import com.bytecause.lenslex.BuildConfig
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

private const val API_KEY = BuildConfig.FIREBASE_WEB_API_KEY

interface VerifyOobCodeRestApiService {

    @POST("v1/accounts:resetPassword")
    suspend fun verify(
        @Query("key") apiKey: String = API_KEY,
        @Query("oobCode") oobCode: String
    ): Response<Void>
}