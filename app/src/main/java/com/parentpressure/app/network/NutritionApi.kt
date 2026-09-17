package com.parentpressure.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface NutritionApi {
    @GET("nutrition/daily/{date}")
    suspend fun getDaily(
        @Path("date") date: String,
        @Header("Authorization") authorization: String,
    ): Response<DailyNutritionResponse>

    @POST("nutrition/meals")
    suspend fun createMeal(
        @Body body: CreateMealRequest,
        @Header("Authorization") authorization: String,
    ): Response<CreateMealResponse>

    @DELETE("nutrition/meals/{id}")
    suspend fun deleteMeal(
        @Path("id") mealId: String,
        @Header("Authorization") authorization: String,
    ): Response<Unit>
}
