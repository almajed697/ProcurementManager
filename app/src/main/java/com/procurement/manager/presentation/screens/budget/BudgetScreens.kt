package com.procurement.manager.presentation.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.presentation.components.*
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency
import com.procurement.manager.util.ValidationUtils

// ── Budget List ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBudget: () -> Unit,
    onNavigateToEditBudget: (Long) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var budgetToDelete by remember { mutableStateOf<Budget?>(null) }

    LaunchedEffect(state.successMessage, state.error) {
        if (state.successMessage != null || state.error != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الميزانيات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBudget,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("ميزانية جديدة") },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
                return@Box
            }

            if (state.budgets.isEmpty()) {
                EmptyState("لا توجد ميزانيات\nاضغط + لإضافة ميزانية جديدة")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Summary row
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("عدد الميزانيات", "${state.budgets.size}", Modifier.weight(1f))
                            StatCard("الإجمالي", state.budgets.sumOf { it.totalAmount }.formatCurrency(), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    items(state.budgets, key = { it.id }) { budget ->
                        BudgetCard(
                            budget = budget,
                            onEdit = { onNavigateToEditBudget(budget.id) },
                            onDelete = { budgetToDelete = budget }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            // Snackbar messages
            state.successMessage?.let { msg ->
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(msg) }
            }
            state.error?.let { err ->
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp), containerColor = MaterialTheme.colorScheme.errorContainer) { Text(err) }
            }
        }
    }

    budgetToDelete?.let { b ->
        ConfirmDeleteDialog(
            message = "هل تريد حذف ميزانية \"${b.name}\"؟\nسيتم حذف جميع الوصولات المرتبطة بها.",
            onConfirm = { viewModel.deleteBudget(b); budgetToDelete = null },
            onDismiss = { budgetToDelete = null }
        )
    }
}

@Composable
fun BudgetCard(budget: Budget, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(budget.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "تعديل", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "حذف", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            BudgetProgressBar(spent = budget.spentAmount, total = budget.totalAmount, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LabelValue("المتبقي", budget.remainingAmount.formatCurrency(), color = if (budget.remainingAmount >= 0) SuccessGreen else ErrorRed)
                LabelValue("المصروف", budget.spentAmount.formatCurrency(), color = ErrorRed)
                LabelValue("الميزانية", budget.totalAmount.formatCurrency(), color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun LabelValue(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ── Add / Edit Budget ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    budgetId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Pre-fill when editing
    val existing = budgetId?.let { id -> state.budgets.find { it.id == id } }

    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var amount by remember(existing) { mutableStateOf(existing?.totalAmount?.toString() ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (budgetId != null) "تعديل الميزانية" else "إضافة ميزانية") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            FormTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "اسم الميزانية",
                isError = nameError != null,
                errorMessage = nameError,
                leadingIcon = { Icon(Icons.Default.Label, null) }
            )

            FormTextField(
                value = amount,
                onValueChange = { amount = it; amountError = null },
                label = "مبلغ الميزانية (ر.س)",
                isError = amountError != null,
                errorMessage = amountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) }
            )

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    nameError   = ValidationUtils.validateBudgetName(name)
                    amountError = ValidationUtils.validateBudgetAmount(amount)
                    if (nameError == null && amountError == null) {
                        viewModel.saveBudget(
                            id = budgetId,
                            name = name.trim(),
                            totalAmount = amount.toDouble()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("حفظ الميزانية", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
