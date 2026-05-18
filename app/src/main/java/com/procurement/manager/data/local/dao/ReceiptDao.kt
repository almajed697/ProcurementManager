package com.procurement.manager.data.local.dao

import androidx.room.*
import com.procurement.manager.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE budgetId = :budgetId ORDER BY date DESC")
    fun getReceiptsByBudget(budgetId: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE id = :id")
    fun getReceiptByIdFlow(id: Long): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getReceiptByNumber(receiptNumber: String): ReceiptEntity?

    @Query("""
        SELECT * FROM receipts 
        WHERE (:query = '' OR receiptNumber LIKE '%' || :query || '%' OR storeName LIKE '%' || :query || '%')
        AND (:budgetId = -1 OR budgetId = :budgetId)
        AND (:fromDate = -1 OR date >= :fromDate)
        AND (:toDate = -1 OR date <= :toDate)
        ORDER BY date DESC
    """)
    fun searchReceipts(
        query: String = "",
        budgetId: Long = -1,
        fromDate: Long = -1,
        toDate: Long = -1
    ): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: Long)

    @Query("SELECT SUM(amount) FROM receipts WHERE budgetId = :budgetId")
    fun getTotalSpentByBudget(budgetId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM receipts")
    fun getTotalSpent(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM receipts")
    fun getReceiptCount(): Flow<Int>

    @Query("""
        SELECT * FROM receipts 
        WHERE date >= :fromDate AND date <= :toDate
        ORDER BY date DESC
    """)
    suspend fun getReceiptsByDateRange(fromDate: Long, toDate: Long): List<ReceiptEntity>

    @Query("SELECT * FROM receipts WHERE budgetId = :budgetId ORDER BY date DESC")
    suspend fun getReceiptsByBudgetSync(budgetId: Long): List<ReceiptEntity>
}
