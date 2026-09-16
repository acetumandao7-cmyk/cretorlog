package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ReportApi {
    suspend fun getSummary(token: String): DashboardSummaryResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.getDashboardSummary(
                authorization = "Bearer $token"
            )
        }
}
