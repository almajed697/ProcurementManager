package com.procurement.manager.presentation.screens.receipt

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.procurement.manager.data.repository.BudgetRepository
import com.procurement.manager.data.repository.ReceiptRepository
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.OcrResult
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.util.ImageUtils
import com.procurement.manager.util.OcrHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ReceiptUiState(
    val receipts: List<Receipt> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val selectedBudgetId: Long = -1L,
    val fromDate: Long = -1L,
    val toDate: Long = -1L,
    val isOcrProcessing: Boolean = false,
    val ocrResult: OcrResult? = null
)

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState(isLoading = true))
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    private val searchQuery   = MutableStateFlow("")
    private val selectedBudgetId = MutableStateFlow(-1L)
    private val fromDate      = MutableStateFlow(-1L)
    private val toDate        = MutableStateFlow(-1L)

    init {
        loadBudgets()
        collectReceipts()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            budgetRepository.getAllBudgets().collect { list ->
                _uiState.update { it.copy(budgets = list) }
            }
        }
    }

    private fun collectReceipts() {
        viewModelScope.launch {
            combine(searchQuery, selectedBudgetId, fromDate, toDate) { q, b, f, t -> listOf(q, b.toString(), f.toString(), t.toString()) }
                .debounce(300)
                .flatMapLatest {
                    receiptRepository.searchReceipts(
                        query    = searchQuery.value,
                        budgetId = selectedBudgetId.value,
                        fromDate = fromDate.value,
                        toDate   = toDate.value
                    )
                }
                .collect { list ->
                    _uiState.update { it.copy(receipts = list, isLoading = false) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onBudgetFilterChange(budgetId: Long) {
        selectedBudgetId.value = budgetId
        _uiState.update { it.copy(selectedBudgetId = budgetId) }
    }

    fun onDateRangeChange(from: Long, to: Long) {
        fromDate.value = from
        toDate.value   = to
        _uiState.update { it.copy(fromDate = from, toDate = to) }
    }

    fun clearFilters() {
        searchQuery.value    = ""
        selectedBudgetId.value = -1L
        fromDate.value       = -1L
        toDate.value         = -1L
        _uiState.update { it.copy(searchQuery = "", selectedBudgetId = -1L, fromDate = -1L, toDate = -1L) }
    }

    fun saveReceipt(
        id: Long?,
        receiptNumber: String,
        storeName: String,
        date: Long,
        amount: Double,
        notes: String,
        budgetId: Long,
        imagePath: String?
    ) {
        viewModelScope.launch {
            try {
                // Check for duplicate receipt number
                val existing = receiptRepository.getReceiptByNumber(receiptNumber)
                if (existing != null && existing.id != (id ?: 0L)) {
                    _uiState.update { it.copy(error = "رقم الوصل موجود مسبقًا: $receiptNumber") }
                    return@launch
                }

                val receipt = Receipt(
                    id = id ?: 0L,
                    receiptNumber = receiptNumber,
                    storeName = storeName,
                    date = date,
                    amount = amount,
                    notes = notes,
                    budgetId = budgetId,
                    imagePath = imagePath
                )
                if (id != null && id > 0) {
                    receiptRepository.updateReceipt(receipt)
                    _uiState.update { it.copy(successMessage = "تم تحديث الوصل بنجاح") }
                } else {
                    receiptRepository.insertReceipt(receipt)
                    _uiState.update { it.copy(successMessage = "تم إضافة الوصل بنجاح") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "حدث خطأ: ${e.message}") }
            }
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                receipt.imagePath?.let { ImageUtils.deleteImageFile(it) }
                receiptRepository.deleteReceipt(receipt)
                _uiState.update { it.copy(successMessage = "تم حذف الوصل") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "خطأ في الحذف: ${e.message}") }
            }
        }
    }

    fun getReceiptById(id: Long): Flow<Receipt?> = receiptRepository.getReceiptByIdFlow(id)

    fun processOcr(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrProcessing = true, ocrResult = null) }
            try {
                val helper = OcrHelper(context)
                val result = helper.extractTextFromUri(uri)
                _uiState.update { it.copy(isOcrProcessing = false, ocrResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isOcrProcessing = false, error = "فشل استخراج البيانات: ${e.message}") }
            }
        }
    }

    fun saveImageToLocal(context: Context, uri: Uri, receiptNumber: String): String? {
        return try {
            val destFile = ImageUtils.createReceiptImageFile(context, receiptNumber)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null, ocrResult = null) }
    }
}
