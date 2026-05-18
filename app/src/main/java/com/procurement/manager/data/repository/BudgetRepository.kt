package com.procurement.manager.data.repository

import com.procurement.manager.data.local.dao.BudgetDao
import com.procurement.manager.data.local.dao.ReceiptDao
import com.procurement.manager.data.local.entity.toDomain
import com.procurement.manager.data.local.entity.toEntity
import com.procurement.manager.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun getBudgetById(id: Long): Budget?
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    suspend fun deleteBudgetById(id: Long)
    fun getTotalBudgetAmount(): Flow<Double>
}

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val receiptDao: ReceiptDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { entity ->
                entity.toDomain(spentAmount = 0.0)
            }
        }
    }

    override suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)?.toDomain()
    }

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    override suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    override fun getTotalBudgetAmount(): Flow<Double> {
        return budgetDao.getTotalBudgetAmount().map { it ?: 0.0 }
    }
}
