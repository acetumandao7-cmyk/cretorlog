package com.example.creatorlog

data class ProtectedResponse(
    val success: Boolean,
    val message: String,
    val user: ProtectedUser
)

data class ProtectedUser(
    val id: Int,
    val email: String
)