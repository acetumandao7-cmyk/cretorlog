package com.example.creatorlog

data class DashboardSummaryResponse(
    val success: Boolean,
    val message: String = "",
    val summary: DashboardSummary? = null
)

data class DashboardSummary(
    val total_clients: Int = 0,
    val total_projects: Int = 0,
    val active_projects: Int = 0,
    val completed_projects: Int = 0,
    val total_revenue: Double = 0.0,
    val total_paid: Double = 0.0,
    val total_balance: Double = 0.0
)
