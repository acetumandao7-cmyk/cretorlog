package com.example.creatorlog

data class ProjectApiItem(
    val id: Int,
    val client_id: Int,
    val title: String,
    val project_type: String?,
    val event_date: String?,
    val event_location: String?,
    val deadline: String?,
    val status: String?
)

data class ProjectListResponse(
    val success: Boolean,
    val message: String = "",
    val projects: List<ProjectApiItem>?
)

data class ProjectMutationResponse(
    val success: Boolean,
    val message: String
)

data class ProjectCreateRequest(
    val client_id: Int,
    val title: String,
    val project_type: String?,
    val event_date: String?,
    val event_location: String?,
    val deadline: String?,
    val status: String?
)

data class ProjectUpdateRequest(
    val client_id: Int,
    val title: String,
    val project_type: String?,
    val event_date: String?,
    val event_location: String?,
    val deadline: String?,
    val status: String?
)
