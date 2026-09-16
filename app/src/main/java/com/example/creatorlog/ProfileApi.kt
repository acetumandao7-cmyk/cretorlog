package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileApi {

    suspend fun getProfile(
        token: String
    ): ProfileResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.getProfile(
                authorization = "Bearer $token"
            )
        }
    }

    suspend fun updateProfile(
        token: String,
        fullName: String,
        email: String
    ): ProfileUpdateResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.updateProfile(
                authorization = "Bearer $token",
                request = ProfileUpdateRequest(
                    full_name = fullName,
                    email = email
                )
            )
        }
    }
}