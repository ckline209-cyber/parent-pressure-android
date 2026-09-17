package com.parentpressure.app.network

import com.google.gson.annotations.SerializedName

// Postgres DECIMAL columns are returned by node-pg (and therefore serialized) as JSON strings,
// e.g. "405.00", not numbers - so these fields are String to match the wire format exactly.
data class FoodDto(
    val id: String,
    @SerializedName("meal_id") val mealId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("usda_food_id") val usdaFoodId: String?,
    @SerializedName("serving_size_grams") val servingSizeGrams: String?,
    @SerializedName("servings_count") val servingsCount: String?,
    @SerializedName("calories_per_serving") val caloriesPerServing: String?,
    @SerializedName("protein_per_serving") val proteinPerServing: String?,
    @SerializedName("carbs_per_serving") val carbsPerServing: String?,
    @SerializedName("fat_per_serving") val fatPerServing: String?,
)

data class MealDto(
    val id: String,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("meal_date") val mealDate: String,
    @SerializedName("total_calories") val totalCalories: String?,
    @SerializedName("total_protein_g") val totalProteinG: String?,
    @SerializedName("total_carbs_g") val totalCarbsG: String?,
    @SerializedName("total_fat_g") val totalFatG: String?,
    val notes: String?,
    val foods: List<FoodDto>,
)

// Computed server-side via plain JS number addition, so these come back as real JSON numbers.
data class NutritionTotalsDto(
    val calories: Double,
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fat_g") val fatG: Double,
)

data class DailyNutritionResponse(
    val date: String,
    val meals: List<MealDto>,
    val totals: NutritionTotalsDto,
)

data class FoodInput(
    @SerializedName("food_name") val foodName: String,
    @SerializedName("servings_count") val servingsCount: Double,
    @SerializedName("calories_per_serving") val caloriesPerServing: Double,
    @SerializedName("protein_per_serving") val proteinPerServing: Double,
    @SerializedName("carbs_per_serving") val carbsPerServing: Double,
    @SerializedName("fat_per_serving") val fatPerServing: Double,
)

data class CreateMealRequest(
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("meal_date") val mealDate: String,
    val notes: String?,
    val foods: List<FoodInput>,
)

data class CreateMealResponse(
    val meal: MealDto,
)
