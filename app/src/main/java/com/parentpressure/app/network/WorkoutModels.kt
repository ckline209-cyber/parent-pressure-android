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
