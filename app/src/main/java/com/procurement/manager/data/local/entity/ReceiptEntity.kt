package com.procurement.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["budgetId"]),
        Index(value = ["receiptNumber"], unique = true)
    ]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val storeName: String,
    val date: Long,
    val amount: Double,
    val notes: String = "",
    val budgetId: Long,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
