package com.procurement.manager.domain.model

data class Budget(
    val id: Long = 0,
    val name: String,
    val totalAmount: Double,
    val spentAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double get() = totalAmount - spentAmount
    val spentPercentage: Float get() = if (totalAmount > 0) (spentAmount / totalAmount).toFloat() else 0f
}

data class Receipt(
    val id: Long = 0,
    val receiptNumber: String,
    val storeName: String,
    val date: Long,
    val amount: Double,
    val notes: String = "",
    val budgetId: Long,
    val budgetName: String = "",
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class DashboardStats(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val budgetCount: Int = 0,
    val receiptCount: Int = 0
) {
    val remaining: Double get() = totalBudget - totalSpent
    val spentPercentage: Float get() = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
}

data class OcrResult(
    val receiptNumber: String? = null,
    val storeName: String? = null,
    val amount: Double? = null,
    val date: Long? = null,
    val rawText: String = ""
)
