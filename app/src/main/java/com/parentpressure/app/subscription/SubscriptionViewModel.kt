package com.parentpressure.app.subscription

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.auth.TokenStore
import com.parentpressure.app.billing.BillingConstants
import com.parentpressure.app.billing.BillingManager
import com.parentpressure.app.billing.BillingPurchaseResult
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.SubscriptionStatusDto
import com.parentpressure.app.network.VerifyPurchaseRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val status: SubscriptionStatusDto? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val managementUrl: String? = null,
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val subscriptionApi = ApiClient.subscriptionApi
    private val billingManager = BillingManager(application)

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        billingManager.connect()
        viewModelScope.launch {
            billingManager.purchaseResults.collect { result ->
                when (result) {
                    is BillingPurchaseResult.Success -> verifyPurchase(result.purchaseToken)
                    is BillingPurchaseResult.Failed ->
                        _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = result.message)
                    BillingPurchaseResult.Cancelled ->
                        _uiState.value = _uiState.value.copy(isSubmitting = false)
                }
            }
        }
    }

    fun loadStatus() {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = subscriptionApi.getStatus("Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isLoading = false, status = body)
                } else {
                    _uiState.value.copy(isLoading = false, errorMessage = "Failed to load subscription (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to load subscription")
            }
        }
    }

    // plan is "monthly" or "yearly" - maps to BillingConstants base plan IDs.
    fun upgrade(activity: Activity, plan: String) {
        val basePlanId = when (plan) {
            "monthly" -> BillingConstants.BASE_PLAN_MONTHLY
            "yearly" -> BillingConstants.BASE_PLAN_YEARLY
            else -> return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
        billingManager.connect {
            viewModelScope.launch {
                val result = billingManager.queryProductDetails()
                val productDetails = result.productDetailsList?.firstOrNull()
                if (productDetails == null) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitError = "Subscription product not found - has it been set up in Play Console yet?",
                    )
                    return@launch
                }
                val launched = billingManager.launchPurchaseFlow(activity, productDetails, basePlanId)
                if (!launched) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitError = "No matching base plan ($basePlanId) found for this product",
                    )
                }
            }
        }
    }

    private fun verifyPurchase(purchaseToken: String) {
        val token = tokenStore.getToken() ?: return

        viewModelScope.launch {
            try {
                val response = subscriptionApi.verifyPurchase(VerifyPurchaseRequest(purchaseToken), "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isSubmitting = false, status = body.subscription)
                } else {
                    _uiState.value.copy(isSubmitting = false, submitError = "Failed to verify purchase (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to verify purchase")
            }
        }
    }

    // Fetches the Play Store subscription management deep link; the screen is
    // responsible for opening it (an actual cancel happens in the Play Store, not here).
    fun requestManagementUrl() {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
        viewModelScope.launch {
            try {
                val response = subscriptionApi.getCancelUrl("Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isSubmitting = false, managementUrl = body.managementUrl)
                } else {
                    _uiState.value.copy(isSubmitting = false, submitError = "Failed to open subscription management (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to open subscription management")
            }
        }
    }

    fun managementUrlHandled() {
        _uiState.value = _uiState.value.copy(managementUrl = null)
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
