package com.parentpressure.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// Subscription product/base-plan IDs as configured in Google Play Console under
// Monetize > Products > Subscriptions. These must match exactly - there is no product
// or base plan in Play Console yet, so this is a placeholder naming decision; update
// here if different names are chosen when the products are actually created.
object BillingConstants {
    const val SUBSCRIPTION_PRODUCT_ID = "premium_subscription"
    const val BASE_PLAN_MONTHLY = "monthly"
    const val BASE_PLAN_YEARLY = "yearly"
}

sealed class BillingPurchaseResult {
    data class Success(val purchaseToken: String) : BillingPurchaseResult()
    data class Failed(val message: String) : BillingPurchaseResult()
    data object Cancelled : BillingPurchaseResult()
}

// Thin wrapper around BillingClient. Deliberately does NOT acknowledge purchases itself -
// the backend acknowledges only after verifying the purchase token against Google's own
// servers, so entitlement and acknowledgement happen in the same trusted place.
class BillingManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _purchaseResults = MutableSharedFlow<BillingPurchaseResult>(extraBufferCapacity = 1)
    val purchaseResults: SharedFlow<BillingPurchaseResult> = _purchaseResults

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                val result = if (purchase != null) {
                    BillingPurchaseResult.Success(purchase.purchaseToken)
                } else {
                    BillingPurchaseResult.Failed("No completed purchase was returned")
                }
                scope.launch { _purchaseResults.emit(result) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch { _purchaseResults.emit(BillingPurchaseResult.Cancelled) }
            }
            else -> {
                scope.launch { _purchaseResults.emit(BillingPurchaseResult.Failed(billingResult.debugMessage)) }
            }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private var isConnected = false

    fun connect(onReady: () -> Unit = {}) {
        if (isConnected) {
            onReady()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                if (isConnected) onReady()
            }

            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    suspend fun queryProductDetails(): ProductDetailsResult {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BillingConstants.SUBSCRIPTION_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        return billingClient.queryProductDetails(params)
    }

    // basePlanId is BillingConstants.BASE_PLAN_MONTHLY or BASE_PLAN_YEARLY.
    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, basePlanId: String): Boolean {
        val offer = productDetails.subscriptionOfferDetails?.firstOrNull { it.basePlanId == basePlanId }
            ?: return false

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offer.offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
        return true
    }

    fun endConnection() {
        billingClient.endConnection()
        isConnected = false
    }
}
