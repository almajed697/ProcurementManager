package com.procurement.manager.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procurement.manager.data.repository.BudgetRepository
import com.procurement.manager.data.repository.ReceiptRepository
import com.procurement.manager.domain.model.Budget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState(isLoading = true))
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            combine(
                budgetRepository.getAllBudgets(),
                receiptRepository.getAllReceipts()
            ) { budgets, receipts ->
                budgets.map { budget ->
                    val spent = receipts.filter { it.budgetId == budget.id }.sumOf { it.amount }
                    budget.copy(spentAmount = spent)
                }
            }.collect { enriched ->
                _uiState.update { it.copy(budgets = enriched, isLoading = false) }
            }
        }
    }

    fun saveBudget(id: Long?, name: String, totalAmount: Double) {
        viewModelScope.launch {
            try {
                if (id != null && id > 0) {
                    val existing = budgetRepository.getBudgetById(id) ?: return@launch
                    budgetRepository.updateBudget(existing.copy(name = name, totalAmount = totalAmount))
                    _uiState.update { it.copy(successMessage = "تم تحديث الميزانية بنجاح") }
                } else {
                    budgetRepository.insertBudget(Budget(name = name, totalAmount = totalAmount))
                    _uiState.update { it.copy(successMessage = "تم إضافة الميزانية بنجاح") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "حدث خطأ: ${e.message}") }
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budget)
                _uiState.update { it.copy(successMessage = "تم حذف الميزانية") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "خطأ في الحذف: ${e.message}") }
            }
        }
    }

    fun getBudgetById(id: Long): Flow<Budget?> {
        return budgetRepository.getAllBudgets().map { list -> list.find { it.id == id } }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
