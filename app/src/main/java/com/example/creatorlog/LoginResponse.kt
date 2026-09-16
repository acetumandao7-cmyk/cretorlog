package com.example.creatorlog

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: LoginUser?
)

data class LoginUser(
    val id: Int,
    val full_name: String,
    val email: String
)