package com.parentpressure.app.network

import com.google.gson.annotations.SerializedName

data class ExerciseDto(
    val id: String,
    @SerializedName("workout_id") val workoutId: String,
    val name: String,
    @SerializedName("muscle_groups") val muscleGroups: List<String>,
    @SerializedName("equipment_needed") val equipmentNeeded: String?,
    @SerializedName("order_in_workout") val orderInWorkout: Int,
    @SerializedName("target_sets") val targetSets: Int?,
    @SerializedName("target_reps") val targetReps: Int?,
    @SerializedName("rest_seconds") val restSeconds: Int?,
)

data class WorkoutDto(
    val id: String,
    val name: String,
    val description: String?,
    @SerializedName("difficulty_level") val difficultyLevel: String?,
    @SerializedName("frequency_per_week") val frequencyPerWeek: Int?,
    val exercises: List<ExerciseDto>,
)

data class WorkoutsResponse(
    val workouts: List<WorkoutDto>,
)

data class UserWorkoutDto(
    val id: String,
    @SerializedName("workout_id") val workoutId: String,
    @SerializedName("assigned_date") val assignedDate: String,
)

data class StartWorkoutResponse(
    val userWorkout: UserWorkoutDto,
)

data class UserWorkoutDetailDto(
    val id: String,
    @SerializedName("workout_id") val workoutId: String,
    @SerializedName("assigned_date") val assignedDate: String,
    @SerializedName("completed_date") val completedDate: String?,
    val notes: String?,
    @SerializedName("workout_name") val workoutName: String,
)

// weight_used_kg is a Postgres DECIMAL, serialized as a JSON string (e.g. "60.00") - see WorkoutModels precedent.
data class ExerciseLogDto(
    val id: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("sets_completed") val setsCompleted: Int?,
    @SerializedName("reps_per_set") val repsPerSet: List<Int>?,
    @SerializedName("weight_used_kg") val weightUsedKg: String?,
    @SerializedName("rest_taken_seconds") val restTakenSeconds: Int?,
    val rpe: Int?,
    val notes: String?,
    @SerializedName("logged_at") val loggedAt: String,
)

data class StartedWorkoutResponse(
    val userWorkout: UserWorkoutDetailDto,
    val exercises: List<ExerciseDto>,
    val logs: List<ExerciseLogDto>,
)

data class LogExerciseRequest(
    @SerializedName("user_workout_id") val userWorkoutId: String,
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("sets_completed") val setsCompleted: Int,
    @SerializedName("reps_per_set") val repsPerSet: List<Int>,
    @SerializedName("weight_used_kg") val weightUsedKg: Double?,
    @SerializedName("rest_taken_seconds") val restTakenSeconds: Int?,
    val rpe: Int?,
    val notes: String?,
)

data class LogExerciseResponse(
    val log: ExerciseLogDto,
)

data class CompleteWorkoutResponse(
    val userWorkout: UserWorkoutDetailDto,
)

// last_weight_kg/suggested_weight_kg are computed server-side via JS Number arithmetic (not raw
// DECIMAL columns), so unlike ExerciseLogDto.weightUsedKg these come back as real JSON numbers.
data class ProgressionSuggestionDto(
    @SerializedName("exercise_id") val exerciseId: String,
    @SerializedName("has_history") val hasHistory: Boolean,
    @SerializedName("last_weight_kg") val lastWeightKg: Double?,
    @SerializedName("last_reps_per_set") val lastRepsPerSet: List<Int>?,
    @SerializedName("last_rpe") val lastRpe: Int?,
    @SerializedName("suggested_weight_kg") val suggestedWeightKg: Double?,
    @SerializedName("suggested_reps") val suggestedReps: Int?,
    val rationale: String,
)

data class PersonalizeRequest(
    val soreness: String?,
    val notes: String?,
)

data class PersonalizedSuggestionDto(
    @SerializedName("suggested_weight_kg") val suggestedWeightKg: Double?,
    @SerializedName("suggested_reps") val suggestedReps: Int?,
    val adjusted: Boolean,
    val rationale: String,
)

data class PersonalizeResponse(
    @SerializedName("exercise_id") val exerciseId: String,
    val base: ProgressionSuggestionDto,
    val personalized: PersonalizedSuggestionDto?,
    @SerializedName("ai_available") val aiAvailable: Boolean,
)
