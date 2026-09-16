package com.example.creatorlog

data class ProjectBalanceResponse(
    val success: Boolean,
    val message: String = "",
    val project: ProjectBalanceProject? = null,
    val finance: ProjectFinance? = null
)

data class ProjectBalanceProject(
    val id: Int,
    val title: String
)

data class ProjectFinance(
    val total_fee: Double = 0.0,
    val total_paid: Double = 0.0,
    val remaining_balance: Double = 0.0
)

data class PaymentApiItem(
    val id: Int,
    val project_id: Int,
    val amount: Double,
    val payment_method: String?,
    val notes: String?,
    val paid_at: String?
)

data class PaymentListResponse(
    val success: Boolean,
    val message: String = "",
    val payments: List<PaymentApiItem>?
)

data class FinanceMutationResponse(
    val success: Boolean,
    val message: String,
    val total_fee: Double? = null
)

data class FinanceRequest(
    val total_fee: Double
)

data class PaymentCreateRequest(
    val amount: Double,
    val payment_method: String? = null,
    val notes: String? = null
)

data class PaymentCreateResponse(
    val success: Boolean,
    val message: String,
    val payment: PaymentApiItem? = null
)
