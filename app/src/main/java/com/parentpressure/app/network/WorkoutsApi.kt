package com.parentpressure.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkoutsApi {
    @GET("workouts")
    suspend fun getWorkouts(@Header("Authorization") authorization: String): Response<WorkoutsResponse>

    @POST("workouts/{id}/start")
    suspend fun startWorkout(
        @Path("id") workoutId: String,
        @Header("Authorization") authorization: String,
    ): Response<StartWorkoutResponse>

    @GET("workouts/started/{id}")
    suspend fun getStartedWorkout(
        @Path("id") userWorkoutId: String,
        @Header("Authorization") authorization: String,
    ): Response<StartedWorkoutResponse>

    @POST("workouts/logs")
    suspend fun logExercise(
        @Body body: LogExerciseRequest,
        @Header("Authorization") authorization: String,
    ): Response<LogExerciseResponse>

    @PATCH("workouts/started/{id}/complete")
    suspend fun completeWorkout(
        @Path("id") userWorkoutId: String,
        @Header("Authorization") authorization: String,
    ): Response<CompleteWorkoutResponse>

    @GET("workouts/exercises/{id}/progression")
    suspend fun getProgression(
        @Path("id") exerciseId: String,
        @Header("Authorization") authorization: String,
    ): Response<ProgressionSuggestionDto>
}
