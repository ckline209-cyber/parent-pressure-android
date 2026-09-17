package com.parentpressure.app.subscription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.auth.TokenStore
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.SubscriptionStatusDto
import com.parentpressure.app.network.UpgradeRequest
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
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val subscriptionApi = ApiClient.subscriptionApi

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

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

    fun upgrade(plan: String) {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
        viewModelScope.launch {
            try {
                val response = subscriptionApi.upgrade(UpgradeRequest(plan, googlePlayOrderId = null), "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isSubmitting = false, status = body.subscription)
                } else {
                    _uiState.value.copy(isSubmitting = false, submitError = "Failed to upgrade (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to upgrade")
            }
        }
    }

    fun cancel() {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
        viewModelScope.launch {
            try {
                val response = subscriptionApi.cancel("Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isSubmitting = false, status = body.subscription)
                } else {
                    _uiState.value.copy(isSubmitting = false, submitError = "Failed to cancel (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to cancel")
            }
        }
    }
}
