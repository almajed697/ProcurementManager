package com.procurement.manager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.procurement.manager.data.local.dao.BudgetDao
import com.procurement.manager.data.local.dao.ReceiptDao
import com.procurement.manager.data.local.entity.BudgetEntity
import com.procurement.manager.data.local.entity.ReceiptEntity

@Database(
    entities = [BudgetEntity::class, ReceiptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ProcurementDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun receiptDao(): ReceiptDao

    companion object {
        const val DATABASE_NAME = "procurement_db"
    }
}
