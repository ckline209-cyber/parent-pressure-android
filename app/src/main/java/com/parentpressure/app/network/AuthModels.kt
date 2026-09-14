package com.parentpressure.app.network

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String?,
    val lastName: String?,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class UserDto(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val subscriptionTier: String,
)

data class AuthResponse(
    val accessToken: String,
    val user: UserDto,
)

data class ErrorResponse(
    val error: String?,
    val message: String?,
)
