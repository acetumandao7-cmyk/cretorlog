package com.example.creatorlog

data class ProfileUser(
    val id: Int,
    val full_name: String,
    val email: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String = "",
    val user: ProfileUser? = null
)

data class ProfileUpdateRequest(
    val full_name: String,
    val email: String
)

data class ProfileUpdateResponse(
    val success: Boolean,
    val message: String = "",
    val user: ProfileUser? = null,
    val token: String? = null
)