package com.parentpressure.app.nutrition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.auth.TokenStore
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.CreateMealRequest
import com.parentpressure.app.network.FoodInput
import com.parentpressure.app.network.MealDto
import com.parentpressure.app.network.NutritionTotalsDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

data class NutritionUiState(
    val date: String = today(),
    val isLoading: Boolean = false,
    val meals: List<MealDto> = emptyList(),
    val totals: NutritionTotalsDto = NutritionTotalsDto(0.0, 0.0, 0.0, 0.0),
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val nutritionApi = ApiClient.nutritionApi

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    fun loadDaily() {
        val token = tokenStore.getToken() ?: return
        val date = _uiState.value.date

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = nutritionApi.getDaily(date, "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(isLoading = false, meals = body.meals, totals = body.totals)
                } else {
                    _uiState.value.copy(isLoading = false, errorMessage = "Failed to load nutrition (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to load nutrition")
            }
        }
    }

    fun logFood(
        mealType: String,
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        servings: Double,
    ) {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isSubmitting = true, submitError = null)
        viewModelScope.launch {
            try {
                val request = CreateMealRequest(
                    mealType = mealType,
                    mealDate = _uiState.value.date,
                    notes = null,
                    foods = listOf(
                        FoodInput(
                            foodName = foodName,
                            servingsCount = servings,
                            caloriesPerServing = calories,
                            proteinPerServing = protein,
                            carbsPerServing = carbs,
                            fatPerServing = fat,
                        )
                    ),
                )
                val response = nutritionApi.createMeal(request, "Bearer $token")
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    loadDaily()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submitError = "Failed to log food (${response.code()})",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitError = e.message ?: "Failed to log food")
            }
        }
    }

    fun deleteMeal(mealId: String) {
        val token = tokenStore.getToken() ?: return
        viewModelScope.launch {
            try {
                val response = nutritionApi.deleteMeal(mealId, "Bearer $token")
                if (response.isSuccessful) {
                    loadDaily()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete meal (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to delete meal")
            }
        }
    }

    fun clearSubmitError() {
        _uiState.value = _uiState.value.copy(submitError = null)
    }
}
