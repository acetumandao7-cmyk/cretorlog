package com.example.creatorlog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FinanceApi {

    suspend fun getBalance(token: String, projectId: Int): ProjectBalanceResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.getProjectBalance(
                authorization = "Bearer $token",
                id = projectId
            )
        }

    suspend fun updateFee(token: String, projectId: Int, totalFee: Double): FinanceMutationResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.updateProjectFinance(
                authorization = "Bearer $token",
                id = projectId,
                request = FinanceRequest(total_fee = totalFee)
            )
        }

    suspend fun getPayments(token: String, projectId: Int): PaymentListResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.getProjectPayments(
                authorization = "Bearer $token",
                id = projectId
            )
        }

    suspend fun createPayment(
        token: String,
        projectId: Int,
        amount: Double,
        paymentMethod: String? = null,
        notes: String? = null
    ): PaymentCreateResponse =
        withContext(Dispatchers.IO) {
            ApiClient.apiService.createProjectPayment(
                authorization = "Bearer $token",
                id = projectId,
                request = PaymentCreateRequest(
                    amount = amount,
                    payment_method = paymentMethod,
                    notes = notes
                )
            )
        }
}
