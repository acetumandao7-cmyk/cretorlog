package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

object ProjectApi {

    suspend fun getProjects(token: String): ProjectListResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.getProjects(
                authorization = "Bearer $token"
            )
        }
    }

    suspend fun createProject(
        token: String,
        clientId: Int,
        title: String,
        projectType: String,
        eventDate: String,
        eventLocation: String,
        deadline: String,
        status: String
    ): ProjectMutationResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.createProject(
                authorization = "Bearer $token",
                request = ProjectCreateRequest(
                    client_id = clientId,
                    title = title,
                    project_type = projectType.ifBlank { null },
                    event_date = eventDate.ifBlank { null },
                    event_location = eventLocation.ifBlank { null },
                    deadline = deadline.ifBlank { null },
                    status = status.ifBlank { "Booked" }
                )
            )
        }
    }

    suspend fun updateProject(
        token: String,
        projectId: Int,
        clientId: Int,
        title: String,
        projectType: String,
        eventDate: String,
        eventLocation: String,
        deadline: String,
        status: String
    ): ProjectMutationResponse {
        return withContext(Dispatchers.IO) {
            ApiClient.apiService.updateProject(
                id = projectId,
                authorization = "Bearer $token",
                request = ProjectUpdateRequest(
                    client_id = clientId,
                    title = title,
                    project_type = projectType.ifBlank { null },
                    event_date = eventDate.ifBlank { null },
                    event_location = eventLocation.ifBlank { null },
                    deadline = deadline.ifBlank { null },
                    status = status.ifBlank { "Booked" }
                )
            )
        }
    }

    fun apiDate(displayDate: String): String {
        if (displayDate.isBlank()) return ""
        return try {
            val input = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            input.isLenient = false
            val output = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            output.format(input.parse(displayDate)!!)
        } catch (_: Exception) {
            displayDate.take(10)
        }
    }

    fun displayDate(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return ""
        return try {
            val clean = apiDate.take(10)
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            input.isLenient = false
            val output = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            output.format(input.parse(clean)!!)
        } catch (_: Exception) {
            apiDate.take(10)
        }
    }
}
