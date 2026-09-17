package com.parentpressure.app.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 is the Android emulator's alias for the host machine's localhost.
    // Swap for your machine's LAN IP when testing on a physical device.
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val gson: Gson = Gson()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val workoutsApi: WorkoutsApi = retrofit.create(WorkoutsApi::class.java)
}
