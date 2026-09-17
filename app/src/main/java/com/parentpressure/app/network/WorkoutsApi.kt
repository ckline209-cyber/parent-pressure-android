package com.parentpressure.app.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
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
}
