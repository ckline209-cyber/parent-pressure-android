package com.parentpressure.app.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.ErrorResponse
import com.parentpressure.app.network.LoginRequest
import com.parentpressure.app.network.RegisterRequest
import com.parentpressure.app.network.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: UserDto? = null,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val authApi = ApiClient.authApi

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean
        get() = tokenStore.getToken() != null

    fun register(email: String, password: String, firstName: String, lastName: String) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val response = authApi.register(
                    RegisterRequest(email, password, firstName.ifBlank { null }, lastName.ifBlank { null })
                )
                handleAuthResponse(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorMessage = e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val response = authApi.login(LoginRequest(email, password))
                handleAuthResponse(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorMessage = e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        tokenStore.clear()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun handleAuthResponse(response: Response<com.parentpressure.app.network.AuthResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            tokenStore.saveToken(body.accessToken)
            _uiState.value = AuthUiState(user = body.user)
        } else {
            val errorMessage = try {
                val errorBody = response.errorBody()?.string()
                ApiClient.gson.fromJson(errorBody, ErrorResponse::class.java)?.message
            } catch (e: Exception) {
                null
            } ?: "Something went wrong (${response.code()})"
            _uiState.value = AuthUiState(errorMessage = errorMessage)
        }
    }
}
