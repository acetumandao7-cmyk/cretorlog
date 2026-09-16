package com.example.creatorlog

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)

data class RegisterUser(
    val id: Int,
    val full_name: String,
    val email: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: RegisterUser?
)