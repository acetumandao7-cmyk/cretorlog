package com.example.creatorlog

data class TaskApiItem(
    val id: Int,
    val project_id: Int,
    val title: String,
    val category: String?,
    val status: String?,
    val due_date: String?,
    val notes: String?,
    val created_at: String?
)

data class TaskListResponse(
    val success: Boolean,
    val message: String = "",
    val tasks: List<TaskApiItem>?
)

data class TaskCreateRequest(
    val title: String,
    val category: String,
    val status: String = "Pending",
    val due_date: String? = null,
    val notes: String? = null
)

data class TaskMutationResponse(
    val success: Boolean,
    val message: String,
    val status: String? = null
)

data class TaskStatusRequest(
    val status: String
)
