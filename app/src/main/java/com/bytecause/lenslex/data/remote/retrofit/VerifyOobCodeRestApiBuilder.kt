package com.bytecause.lenslex.data.remote.retrofit

import retrofit2.Retrofit

class VerifyOobCodeRestApiBuilder {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://identitytoolkit.googleapis.com/")
        .build()

    fun getVerifyOobCodeRestApiService(): VerifyOobCodeRestApiService {
        return retrofit.create(VerifyOobCodeRestApiService::class.java)
    }
}