package com.example.creatorlog

data class NotificationApiItem(
    val id: Int,
    val project_id: Int?,
    val title: String,
    val message: String,
    val type: String?,
    val is_read: Boolean,
    val created_at: String?
)

data class NotificationListResponse(
    val success: Boolean,
    val message: String = "",
    val notifications: List<NotificationApiItem>?
)

data class NotificationMutationResponse(
    val success: Boolean,
    val message: String
)
