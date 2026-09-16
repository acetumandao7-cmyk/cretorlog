package com.example.creatorlog

data class ClientCreateResponse(
    val success: Boolean,
    val message: String,
    val client: ClientApiModel?
)