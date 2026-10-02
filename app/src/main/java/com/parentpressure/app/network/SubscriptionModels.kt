package com.parentpressure.app.network

import com.google.gson.annotations.SerializedName

data class SubscriptionStatusDto(
    @SerializedName("subscription_tier") val subscriptionTier: String,
    @SerializedName("subscription_active") val subscriptionActive: Boolean,
    @SerializedName("subscription_start_date") val subscriptionStartDate: String?,
    @SerializedName("subscription_end_date") val subscriptionEndDate: String?,
)

data class VerifyPurchaseRequest(
    @SerializedName("purchaseToken") val purchaseToken: String,
)

data class SubscriptionResponse(
    val subscription: SubscriptionStatusDto,
)

data class CancelResponse(
    val managementUrl: String,
)
