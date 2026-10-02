package com.parentpressure.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SubscriptionApi {
    @GET("subscription/status")
    suspend fun getStatus(@Header("Authorization") authorization: String): Response<SubscriptionStatusDto>

    @POST("subscription/verify-purchase")
    suspend fun verifyPurchase(
        @Body body: VerifyPurchaseRequest,
        @Header("Authorization") authorization: String,
    ): Response<SubscriptionResponse>

    // Cancellation is driven by the Play Store, not this API - this just returns the
    // deep link into Play Store subscription management.
    @GET("subscription/cancel")
    suspend fun getCancelUrl(@Header("Authorization") authorization: String): Response<CancelResponse>
}
