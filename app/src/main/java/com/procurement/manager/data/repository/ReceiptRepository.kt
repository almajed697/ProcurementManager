package com.procurement.manager.data.repository

import com.procurement.manager.data.local.dao.BudgetDao
import com.procurement.manager.data.local.dao.ReceiptDao
import com.procurement.manager.data.local.entity.ReceiptEntity
import com.procurement.manager.data.local.entity.toDomain
import com.procurement.manager.data.local.entity.toEntity
import com.procurement.manager.domain.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ReceiptRepository {
    fun getAllReceipts(): Flow<List<Receipt>>
    fun getReceiptsByBudget(budgetId: Long): Flow<List<Receipt>>
    fun searchReceipts(query: String, budgetId: Long, fromDate: Long, toDate: Long): Flow<List<Receipt>>
    suspend fun getReceiptById(id: Long): Receipt?
    fun getReceiptByIdFlow(id: Long): Flow<Receipt?>
    suspend fun getReceiptByNumber(receiptNumber: String): Receipt?
    suspend fun insertReceipt(receipt: Receipt): Long
    suspend fun updateReceipt(receipt: Receipt)
    suspend fun deleteReceipt(receipt: Receipt)
    suspend fun deleteReceiptById(id: Long)
    fun getTotalSpent(): Flow<Double>
    fun getTotalSpentByBudget(budgetId: Long): Flow<Double>
    fun getReceiptCount(): Flow<Int>
    suspend fun getReceiptsByDateRange(fromDate: Long, toDate: Long): List<Receipt>
    suspend fun getReceiptsByBudgetSync(budgetId: Long): List<Receipt>
}

@Singleton
class ReceiptRepositoryImpl @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val budgetDao: BudgetDao
) : ReceiptRepository {

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { entities ->
            entities.map { entity ->
                val budgetName = budgetDao.getBudgetById(entity.budgetId)?.name ?: ""
                entity.toDomain(budgetName)
            }
        }
    }

    override fun getReceiptsByBudget(budgetId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByBudget(budgetId).map { entities ->
            val budgetName = budgetDao.getBudgetById(budgetId)?.name ?: ""
            entities.map { it.toDomain(budgetName) }
        }
    }

    override fun searchReceipts(query: String, budgetId: Long, fromDate: Long, toDate: Long): Flow<List<Receipt>> {
        return receiptDao.searchReceipts(query, budgetId, fromDate, toDate).map { entities ->
            entities.map { entity ->
                val budgetName = budgetDao.getBudgetById(entity.budgetId)?.name ?: ""
                entity.toDomain(budgetName)
            }
        }
    }

    override suspend fun getReceiptById(id: Long): Receipt? {
        val entity = receiptDao.getReceiptById(id) ?: return null
        val budgetName = budgetDao.getBudgetById(entity.budgetId)?.name ?: ""
        return entity.toDomain(budgetName)
    }

    override fun getReceiptByIdFlow(id: Long): Flow<Receipt?> {
        return receiptDao.getReceiptByIdFlow(id).map { entity ->
            entity?.let {
                val budgetName = budgetDao.getBudgetById(it.budgetId)?.name ?: ""
                it.toDomain(budgetName)
            }
        }
    }

    override suspend fun getReceiptByNumber(receiptNumber: String): Receipt? {
        val entity = receiptDao.getReceiptByNumber(receiptNumber) ?: return null
        val budgetName = budgetDao.getBudgetById(entity.budgetId)?.name ?: ""
        return entity.toDomain(budgetName)
    }

    override suspend fun insertReceipt(receipt: Receipt): Long {
        return receiptDao.insertReceipt(receipt.toEntity())
    }

    override suspend fun updateReceipt(receipt: Receipt) {
        receiptDao.updateReceipt(receipt.toEntity())
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(receipt.toEntity())
    }

    override suspend fun deleteReceiptById(id: Long) {
        receiptDao.deleteReceiptById(id)
    }

    override fun getTotalSpent(): Flow<Double> {
        return receiptDao.getTotalSpent().map { it ?: 0.0 }
    }

    override fun getTotalSpentByBudget(budgetId: Long): Flow<Double> {
        return receiptDao.getTotalSpentByBudget(budgetId).map { it ?: 0.0 }
    }

    override fun getReceiptCount(): Flow<Int> {
        return receiptDao.getReceiptCount()
    }

    override suspend fun getReceiptsByDateRange(fromDate: Long, toDate: Long): List<Receipt> {
        return receiptDao.getReceiptsByDateRange(fromDate, toDate).map { entity ->
            val budgetName = budgetDao.getBudgetById(entity.budgetId)?.name ?: ""
            entity.toDomain(budgetName)
        }
    }

    override suspend fun getReceiptsByBudgetSync(budgetId: Long): List<Receipt> {
        val budgetName = budgetDao.getBudgetById(budgetId)?.name ?: ""
        return receiptDao.getReceiptsByBudgetSync(budgetId).map { it.toDomain(budgetName) }
    }
}
