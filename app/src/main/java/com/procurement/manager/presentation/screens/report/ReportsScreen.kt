package com.procurement.manager.presentation.screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.procurement.manager.presentation.components.BudgetProgressBar
import com.procurement.manager.presentation.components.StatCard
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    var budgetMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.generatedFile) {
        state.generatedFile?.let { file ->
            viewModel.sharePdf(context, file)
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقارير") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Summary Stats ────────────────────────────────────────
            Text("الملخص الإجمالي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)

            val totalBudget = state.budgets.sumOf { it.totalAmount }
            val totalSpent  = state.receipts.sumOf { it.amount }
            val remaining   = totalBudget - totalSpent

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("الميزانية", totalBudget.formatCurrency(), Modifier.weight(1f))
                StatCard("المصروف", totalSpent.formatCurrency(), Modifier.weight(1f), containerColor = Color(0xFFFFE0E0), contentColor = ErrorRed)
            }
            StatCard("المتبقي", remaining.formatCurrency(), Modifier.fillMaxWidth(), containerColor = Color(0xFFE8F5E9), contentColor = SuccessGreen)

            BudgetProgressBar(spent = totalSpent, total = totalBudget, Modifier.fillMaxWidth())

            HorizontalDivider()

            // ── Per-Budget Summary ───────────────────────────────────
            Text("تفاصيل الميزانيات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)

            state.budgets.forEach { budget ->
                val budgetSpent = state.receipts.filter { it.budgetId == budget.id }.sumOf { it.amount }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(budget.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(budget.totalAmount.formatCurrency(), color = PrimaryBlue, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(4.dp))
                        BudgetProgressBar(spent = budgetSpent, total = budget.totalAmount)
                        Text("عدد الوصولات: ${state.receipts.count { it.budgetId == budget.id }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider()

            // ── PDF Generation ───────────────────────────────────────
            Text("إنشاء التقرير", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)

            // Budget filter
            ExposedDropdownMenuBox(expanded = budgetMenuExpanded, onExpandedChange = { budgetMenuExpanded = it }) {
                OutlinedTextField(
                    value = state.budgets.find { it.id == state.selectedBudgetId }?.name ?: "جميع الميزانيات",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("فلترة حسب الميزانية") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(budgetMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = budgetMenuExpanded, onDismissRequest = { budgetMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("جميع الميزانيات") }, onClick = { viewModel.setFilter(budgetId = -1L); budgetMenuExpanded = false })
                    state.budgets.forEach { b ->
                        DropdownMenuItem(text = { Text(b.name) }, onClick = { viewModel.setFilter(budgetId = b.id); budgetMenuExpanded = false })
                    }
                }
            }

            state.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { viewModel.generatePdf(context) },
                enabled = !state.isGenerating,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("جاري الإنشاء...")
                } else {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(8.dp))
                    Text("إنشاء وتصدير PDF", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
