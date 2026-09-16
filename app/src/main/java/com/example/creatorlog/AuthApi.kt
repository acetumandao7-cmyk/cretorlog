package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthApi {

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): RegisterResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.register(
                RegisterRequest(
                    full_name = fullName,
                    email = email,
                    password = password
                )
            )
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): LoginResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )
        }
    }

    suspend fun getProtected(
        token: String
    ): ProtectedResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.getProtected(
                authorization = "Bearer $token"
            )
        }
    }
}