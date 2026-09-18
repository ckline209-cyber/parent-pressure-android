package com.parentpressure.app.workouts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.auth.TokenStore
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.WorkoutDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val isLoading: Boolean = false,
    val workouts: List<WorkoutDto> = emptyList(),
    val errorMessage: String? = null,
    val startingWorkoutId: String? = null,
    val startedWorkouts: Map<String, String> = emptyMap(),
    val startError: String? = null,
)

class WorkoutsViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val workoutsApi = ApiClient.workoutsApi

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    fun loadWorkouts() {
        val token = tokenStore.getToken()
        if (token == null) {
            _uiState.value = WorkoutsUiState(errorMessage = "Not logged in")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = workoutsApi.getWorkouts("Bearer $token")
                _uiState.value = if (response.isSuccessful && response.body() != null) {
                    WorkoutsUiState(workouts = response.body()!!.workouts)
                } else {
                    WorkoutsUiState(errorMessage = "Failed to load workouts (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutsUiState(errorMessage = e.message ?: "Failed to load workouts")
            }
        }
    }

    fun startWorkout(workoutId: String) {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(startingWorkoutId = workoutId, startError = null)
        viewModelScope.launch {
            try {
                val response = workoutsApi.startWorkout(workoutId, "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    _uiState.value.copy(
                        startingWorkoutId = null,
                        startedWorkouts = _uiState.value.startedWorkouts + (workoutId to body.userWorkout.id),
                    )
                } else {
                    _uiState.value.copy(
                        startingWorkoutId = null,
                        startError = "Failed to start workout (${response.code()})",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    startingWorkoutId = null,
                    startError = e.message ?: "Failed to start workout",
                )
            }
        }
    }
}
