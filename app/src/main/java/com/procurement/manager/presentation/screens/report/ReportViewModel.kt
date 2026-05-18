package com.procurement.manager.presentation.screens.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procurement.manager.data.repository.BudgetRepository
import com.procurement.manager.data.repository.ReceiptRepository
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ReportUiState(
    val receipts: List<Receipt> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val selectedBudgetId: Long = -1L,
    val fromDate: Long = -1L,
    val toDate: Long = -1L,
    val isGenerating: Boolean = false,
    val generatedFile: File? = null,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                budgetRepository.getAllBudgets(),
                receiptRepository.getAllReceipts()
            ) { b, r -> Pair(b, r) }.collect { (b, r) ->
                _uiState.update { it.copy(budgets = b, receipts = r) }
            }
        }
    }

    fun setFilter(budgetId: Long = -1L, fromDate: Long = -1L, toDate: Long = -1L) {
        _uiState.update { it.copy(selectedBudgetId = budgetId, fromDate = fromDate, toDate = toDate) }
    }

    fun generatePdf(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generatedFile = null) }
            try {
                val state = _uiState.value
                val filtered = state.receipts.filter { receipt ->
                    (state.selectedBudgetId == -1L || receipt.budgetId == state.selectedBudgetId) &&
                    (state.fromDate == -1L || receipt.date >= state.fromDate) &&
                    (state.toDate == -1L || receipt.date <= state.toDate)
                }
                val budgetsForReport = if (state.selectedBudgetId != -1L)
                    state.budgets.filter { it.id == state.selectedBudgetId }
                else state.budgets

                val file = PdfGenerator.generateReport(context, filtered, budgetsForReport)
                _uiState.update { it.copy(isGenerating = false, generatedFile = file) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = "خطأ في إنشاء التقرير: ${e.message}") }
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير"))
    }

    fun clearState() {
        _uiState.update { it.copy(generatedFile = null, error = null) }
    }
}
