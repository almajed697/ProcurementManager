package com.procurement.manager.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procurement.manager.data.repository.BudgetRepository
import com.procurement.manager.data.repository.ReceiptRepository
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.DashboardStats
import com.procurement.manager.domain.model.Receipt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val recentReceipts: List<Receipt> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                budgetRepository.getAllBudgets(),
                receiptRepository.getAllReceipts(),
                budgetRepository.getTotalBudgetAmount(),
                receiptRepository.getTotalSpent(),
                receiptRepository.getReceiptCount()
            ) { budgets, receipts, totalBudget, totalSpent, receiptCount ->
                // Enrich budgets with spent amounts
                val enrichedBudgets = budgets.map { budget ->
                    val spent = receipts.filter { it.budgetId == budget.id }.sumOf { it.amount }
                    budget.copy(spentAmount = spent)
                }
                DashboardUiState(
                    stats = DashboardStats(
                        totalBudget  = totalBudget,
                        totalSpent   = totalSpent,
                        budgetCount  = budgets.size,
                        receiptCount = receiptCount
                    ),
                    recentReceipts = receipts.take(5),
                    budgets = enrichedBudgets,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
