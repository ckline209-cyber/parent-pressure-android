package com.parentpressure.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SubscriptionApi {
    @GET("subscription/status")
    suspend fun getStatus(@Header("Authorization") authorization: String): Response<SubscriptionStatusDto>

    @POST("subscription/upgrade")
    suspend fun upgrade(
        @Body body: UpgradeRequest,
        @Header("Authorization") authorization: String,
    ): Response<SubscriptionResponse>

    @POST("subscription/cancel")
    suspend fun cancel(@Header("Authorization") authorization: String): Response<SubscriptionResponse>
}
