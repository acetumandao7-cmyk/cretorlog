package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TaskApi {

    suspend fun getTasks(token: String, projectId: Int): TaskListResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.getProjectTasks(
                authorization = "Bearer $token",
                id = projectId
            )
        }

    suspend fun createTask(
        token: String,
        projectId: Int,
        title: String,
        category: String
    ): TaskMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.createProjectTask(
                authorization = "Bearer $token",
                id = projectId,
                request = TaskCreateRequest(
                    title = title,
                    category = category
                )
            )
        }

    suspend fun updateStatus(
        token: String,
        projectId: Int,
        taskId: Int,
        status: String
    ): TaskMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.updateProjectTaskStatus(
                authorization = "Bearer $token",
                projectId = projectId,
                taskId = taskId,
                request = TaskStatusRequest(status)
            )
        }

    suspend fun deleteTask(
        token: String,
        projectId: Int,
        taskId: Int
    ): TaskMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.deleteProjectTask(
                authorization = "Bearer $token",
                projectId = projectId,
                taskId = taskId
            )
        }
}
