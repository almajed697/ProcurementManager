package com.procurement.manager.presentation.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.presentation.components.*
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency
import com.procurement.manager.util.DateUtils.toDisplayDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToBudgets: () -> Unit,
    onNavigateToReceipts: () -> Unit,
    onNavigateToAddReceipt: () -> Unit,
    onNavigateToReceiptDetail: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("لجنة المشتريات", style = MaterialTheme.typography.titleLarge)
                        Text("لوحة التحكم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToAddReceipt) {
                        Icon(Icons.Default.Add, "إضافة وصل")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Summary Cards ───────────────────────────────────────
            item {
                SectionHeader("الملخص المالي")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "إجمالي الميزانية",
                        value = state.stats.totalBudget.formatCurrency(),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    StatCard(
                        title = "المصروف",
                        value = state.stats.totalSpent.formatCurrency(),
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFFFE0E0),
                        contentColor = ErrorRed
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = "المتبقي",
                        value = state.stats.remaining.formatCurrency(),
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = SuccessGreen
                    )
                    StatCard(
                        title = "عدد الوصولات",
                        value = "${state.stats.receiptCount}",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // ── Overall Progress ────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("نسبة الإنفاق الإجمالية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        BudgetProgressBar(spent = state.stats.totalSpent, total = state.stats.totalBudget, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // ── Budgets Overview ────────────────────────────────────
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("الميزانيات (${state.budgets.size})")
                    TextButton(onClick = onNavigateToBudgets) { Text("عرض الكل") }
                }
            }

            if (state.budgets.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("لا توجد ميزانيات، اضغط لإضافة ميزانية جديدة", modifier = Modifier.padding(16.dp).clickable { onNavigateToBudgets() }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(state.budgets.take(3)) { budget ->
                BudgetSummaryCard(budget = budget, onClick = { onNavigateToBudgets() })
            }

            // ── Recent Receipts ─────────────────────────────────────
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("أحدث الوصولات")
                    TextButton(onClick = onNavigateToReceipts) { Text("عرض الكل") }
                }
            }

            if (state.recentReceipts.isEmpty()) {
                item { Text("لا توجد وصولات", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(state.recentReceipts) { receipt ->
                RecentReceiptItem(receipt = receipt, onClick = { onNavigateToReceiptDetail(receipt.id) })
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BudgetSummaryCard(budget: Budget, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(budget.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(budget.totalAmount.formatCurrency(), style = MaterialTheme.typography.bodyMedium, color = PrimaryBlue)
            }
            Spacer(Modifier.height(6.dp))
            BudgetProgressBar(spent = budget.spentAmount, total = budget.totalAmount)
        }
    }
}

@Composable
fun RecentReceiptItem(receipt: Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(receipt.storeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("# ${receipt.receiptNumber} • ${receipt.date.toDisplayDate()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (receipt.budgetName.isNotBlank()) {
                    Text(receipt.budgetName, style = MaterialTheme.typography.bodySmall, color = SecondaryTeal)
                }
            }
            Text(receipt.amount.formatCurrency(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
    }
}
