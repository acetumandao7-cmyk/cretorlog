package com.example.creatorlog

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/auth/register")
suspend fun register(
    @Body request: RegisterRequest
): RegisterResponse

    @GET("api/protected")
    suspend fun getProtected(
        @Header("Authorization") authorization: String
    ): ProtectedResponse

    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): ProfileResponse

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpdateRequest
    ): ProfileUpdateResponse

    @GET("api/clients")
    suspend fun getClients(
        @Header("Authorization") authorization: String
    ): ClientListResponse

    @POST("api/clients")
    suspend fun createClient(
        @Header("Authorization") authorization: String,
        @Body request: ClientCreateRequest
    ): ClientMutationResponse

    @PUT("api/clients/{id}")
    suspend fun updateClient(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String,
        @Body request: ClientUpdateRequest
    ): ClientMutationResponse

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String
    ): ClientMutationResponse

    @GET("api/projects")
    suspend fun getProjects(
        @Header("Authorization") authorization: String
    ): ProjectListResponse

    @POST("api/projects")
    suspend fun createProject(
        @Header("Authorization") authorization: String,
        @Body request: ProjectCreateRequest
    ): ProjectMutationResponse

    @PUT("api/projects/{id}")
    suspend fun updateProject(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String,
        @Body request: ProjectUpdateRequest
    ): ProjectMutationResponse

    @GET("api/projects/{id}/balance")
    suspend fun getProjectBalance(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): ProjectBalanceResponse

    @PUT("api/projects/{id}/finance")
    suspend fun updateProjectFinance(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: FinanceRequest
    ): FinanceMutationResponse

    @GET("api/projects/{id}/payments")
    suspend fun getProjectPayments(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): PaymentListResponse

    @POST("api/projects/{id}/payments")
    suspend fun createProjectPayment(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: PaymentCreateRequest
    ): PaymentCreateResponse

    @POST("api/projects/{id}/tasks")
    suspend fun createProjectTask(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body request: TaskCreateRequest
    ): TaskMutationResponse

    @GET("api/projects/{id}/tasks")
    suspend fun getProjectTasks(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): TaskListResponse

    @PUT("api/projects/{projectId}/tasks/{taskId}/status")
    suspend fun updateProjectTaskStatus(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Int,
        @Path("taskId") taskId: Int,
        @Body request: TaskStatusRequest
    ): TaskMutationResponse

    @DELETE("api/projects/{projectId}/tasks/{taskId}")
    suspend fun deleteProjectTask(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Int,
        @Path("taskId") taskId: Int
    ): TaskMutationResponse

    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") authorization: String
    ): NotificationListResponse

    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): NotificationMutationResponse

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): NotificationMutationResponse

    @GET("api/projects/dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") authorization: String
    ): DashboardSummaryResponse
}
