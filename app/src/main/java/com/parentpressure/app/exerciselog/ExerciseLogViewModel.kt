package com.parentpressure.app.exerciselog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.parentpressure.app.auth.TokenStore
import com.parentpressure.app.network.ApiClient
import com.parentpressure.app.network.ExerciseDto
import com.parentpressure.app.network.ExerciseLogDto
import com.parentpressure.app.network.LogExerciseRequest
import com.parentpressure.app.network.PersonalizeRequest
import com.parentpressure.app.network.PersonalizeResponse
import com.parentpressure.app.network.ProgressionSuggestionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseLogUiState(
    val isLoading: Boolean = false,
    val workoutName: String = "",
    val isCompleted: Boolean = false,
    val exercises: List<ExerciseDto> = emptyList(),
    val logsByExercise: Map<String, List<ExerciseLogDto>> = emptyMap(),
    val progressionByExercise: Map<String, ProgressionSuggestionDto> = emptyMap(),
    val personalizedByExercise: Map<String, PersonalizeResponse> = emptyMap(),
    val aiLoadingExerciseId: String? = null,
    val aiErrorByExercise: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val loggingExerciseId: String? = null,
    val logError: String? = null,
    val isCompleting: Boolean = false,
)

class ExerciseLogViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val workoutsApi = ApiClient.workoutsApi

    private val _uiState = MutableStateFlow(ExerciseLogUiState())
    val uiState: StateFlow<ExerciseLogUiState> = _uiState.asStateFlow()

    private lateinit var userWorkoutId: String

    fun load(userWorkoutId: String) {
        this.userWorkoutId = userWorkoutId
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = workoutsApi.getStartedWorkout(userWorkoutId, "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    ExerciseLogUiState(
                        workoutName = body.userWorkout.workoutName,
                        isCompleted = body.userWorkout.completedDate != null,
                        exercises = body.exercises,
                        logsByExercise = body.logs.groupBy { it.exerciseId },
                    )
                } else {
                    ExerciseLogUiState(errorMessage = "Failed to load workout (${response.code()})")
                }
                body?.exercises?.forEach { fetchProgression(it.id) }
            } catch (e: Exception) {
                _uiState.value = ExerciseLogUiState(errorMessage = e.message ?: "Failed to load workout")
            }
        }
    }

    // Best-effort - the suggestion is a hint, not part of the critical path, so failures are silent.
    private fun fetchProgression(exerciseId: String) {
        val token = tokenStore.getToken() ?: return
        viewModelScope.launch {
            try {
                val response = workoutsApi.getProgression(exerciseId, "Bearer $token")
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.value = _uiState.value.copy(
                        progressionByExercise = _uiState.value.progressionByExercise + (exerciseId to body),
                    )
                }
            } catch (e: Exception) {
                // Ignored - see comment above.
            }
        }
    }

    fun logSet(exerciseId: String, repsPerSet: List<Int>, weightKg: Double?, rpe: Int?) {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(loggingExerciseId = exerciseId, logError = null)
        viewModelScope.launch {
            try {
                val request = LogExerciseRequest(
                    userWorkoutId = userWorkoutId,
                    exerciseId = exerciseId,
                    setsCompleted = repsPerSet.size,
                    repsPerSet = repsPerSet,
                    weightUsedKg = weightKg,
                    restTakenSeconds = null,
                    rpe = rpe,
                    notes = null,
                )
                val response = workoutsApi.logExercise(request, "Bearer $token")
                val body = response.body()
                _uiState.value = if (response.isSuccessful && body != null) {
                    val updatedLogs = _uiState.value.logsByExercise.toMutableMap()
                    updatedLogs[exerciseId] = (updatedLogs[exerciseId] ?: emptyList()) + body.log
                    fetchProgression(exerciseId)
                    _uiState.value.copy(loggingExerciseId = null, logsByExercise = updatedLogs)
                } else {
                    _uiState.value.copy(loggingExerciseId = null, logError = "Failed to log set (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loggingExerciseId = null, logError = e.message ?: "Failed to log set")
            }
        }
    }

    fun requestAiCoaching(exerciseId: String, soreness: String?, notes: String?) {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(aiLoadingExerciseId = exerciseId)
        viewModelScope.launch {
            try {
                val response = workoutsApi.getPersonalizedProgression(
                    exerciseId,
                    PersonalizeRequest(soreness, notes),
                    "Bearer $token",
                )
                val body = response.body()
                _uiState.value = when {
                    response.code() == 403 -> _uiState.value.copy(
                        aiLoadingExerciseId = null,
                        aiErrorByExercise = _uiState.value.aiErrorByExercise +
                            (exerciseId to "AI coaching requires a Premium subscription."),
                    )

                    response.isSuccessful && body != null -> _uiState.value.copy(
                        aiLoadingExerciseId = null,
                        personalizedByExercise = _uiState.value.personalizedByExercise + (exerciseId to body),
                        aiErrorByExercise = _uiState.value.aiErrorByExercise - exerciseId,
                    )

                    else -> _uiState.value.copy(
                        aiLoadingExerciseId = null,
                        aiErrorByExercise = _uiState.value.aiErrorByExercise +
                            (exerciseId to "Failed to get AI coaching (${response.code()})"),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    aiLoadingExerciseId = null,
                    aiErrorByExercise = _uiState.value.aiErrorByExercise +
                        (exerciseId to (e.message ?: "Failed to get AI coaching")),
                )
            }
        }
    }

    fun completeWorkout() {
        val token = tokenStore.getToken() ?: return

        _uiState.value = _uiState.value.copy(isCompleting = true, logError = null)
        viewModelScope.launch {
            try {
                val response = workoutsApi.completeWorkout(userWorkoutId, "Bearer $token")
                _uiState.value = if (response.isSuccessful) {
                    _uiState.value.copy(isCompleting = false, isCompleted = true)
                } else {
                    _uiState.value.copy(isCompleting = false, logError = "Failed to complete workout (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCompleting = false, logError = e.message ?: "Failed to complete workout")
            }
        }
    }
}
