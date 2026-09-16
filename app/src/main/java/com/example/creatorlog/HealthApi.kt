package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HealthApi {

    suspend fun checkBackend(): HealthResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.getHealth()
        }
    }
}