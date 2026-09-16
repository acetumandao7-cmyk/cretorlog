package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NotificationApi {

    suspend fun getNotifications(token: String): NotificationListResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.getNotifications(
                authorization = "Bearer $token"
            )
        }

    suspend fun markRead(token: String, id: Int): NotificationMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.markNotificationRead(
                authorization = "Bearer $token",
                id = id
            )
        }

    suspend fun delete(token: String, id: Int): NotificationMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.deleteNotification(
                authorization = "Bearer $token",
                id = id
            )
        }
}
