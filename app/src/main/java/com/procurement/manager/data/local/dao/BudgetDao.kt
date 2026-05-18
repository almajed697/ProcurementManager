package com.procurement.manager.data.local.dao

import androidx.room.*
import com.procurement.manager.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetByIdFlow(id: Long): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("SELECT COUNT(*) FROM budgets")
    fun getBudgetCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM budgets")
    fun getTotalBudgetAmount(): Flow<Double?>
}
