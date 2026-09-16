package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ClientApi {

    suspend fun getClients(
        token: String
    ): ClientListResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.getClients(
                authorization = "Bearer $token"
            )
        }
    }

    suspend fun createClient(
        token: String,
        fullName: String,
        email: String?,
        phone: String?,
        address: String?,
        notes: String?
    ): ClientMutationResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.createClient(
                authorization = "Bearer $token",
                request = ClientCreateRequest(
                    full_name = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    notes = notes
                )
            )
        }
    }

    suspend fun updateClient(
        token: String,
        clientId: Int,
        fullName: String,
        email: String?,
        phone: String?,
        address: String?,
        notes: String?
    ): ClientMutationResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.updateClient(
                id = clientId,
                authorization = "Bearer $token",
                request = ClientUpdateRequest(
                    full_name = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    notes = notes
                )
            )
        }
    }

    suspend fun deleteClient(
        token: String,
        clientId: Int
    ): ClientMutationResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.deleteClient(
                id = clientId,
                authorization = "Bearer $token"
            )
        }
    }
}