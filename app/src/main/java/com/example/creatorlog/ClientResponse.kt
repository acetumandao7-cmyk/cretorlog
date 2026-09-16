package com.example.creatorlog

data class ClientResponse(
    val success: Boolean,
    val message: String,
    val clients: List<ClientApiModel>?
)

data class ClientApiModel(
    val id: Int,
    val account_id: Int,
    val full_name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val notes: String?
)