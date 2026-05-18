package com.procurement.manager.data.local.entity

import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.Receipt

fun BudgetEntity.toDomain(spentAmount: Double = 0.0) = Budget(
    id = id,
    name = name,
    totalAmount = totalAmount,
    spentAmount = spentAmount,
    createdAt = createdAt
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    name = name,
    totalAmount = totalAmount,
    createdAt = createdAt
)

fun ReceiptEntity.toDomain(budgetName: String = "") = Receipt(
    id = id,
    receiptNumber = receiptNumber,
    storeName = storeName,
    date = date,
    amount = amount,
    notes = notes,
    budgetId = budgetId,
    budgetName = budgetName,
    imagePath = imagePath,
    createdAt = createdAt
)

fun Receipt.toEntity() = ReceiptEntity(
    id = id,
    receiptNumber = receiptNumber,
    storeName = storeName,
    date = date,
    amount = amount,
    notes = notes,
    budgetId = budgetId,
    imagePath = imagePath,
    createdAt = createdAt
)
