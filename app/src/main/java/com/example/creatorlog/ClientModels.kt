package com.example.creatorlog

data class ClientCreateRequest(
    val full_name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)

data class ClientUpdateRequest(
    val full_name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)

data class ClientApiItem(
    val id: Int,
    val full_name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

data class ClientListResponse(
    val success: Boolean,
    val message: String = "",
    val clients: List<ClientApiItem>? = null
)

data class ClientMutationResponse(
    val success: Boolean,
    val message: String = "",
    val client: ClientApiItem? = null
)