package com.bytecause.lenslex.data.remote.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class VerifyOobCodeRestApiBuilder {

    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }
    private val client = OkHttpClient.Builder().addInterceptor(interceptor)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://identitytoolkit.googleapis.com/")
        .client(client.build())
        .build()

    fun getVerifyOobCodeRestApiService(): VerifyOobCodeRestApiService {
        return retrofit.create(VerifyOobCodeRestApiService::class.java)
    }
}