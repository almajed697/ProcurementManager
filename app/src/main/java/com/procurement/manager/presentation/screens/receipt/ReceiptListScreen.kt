package com.procurement.manager.presentation.screens.receipt

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.presentation.components.*
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency
import com.procurement.manager.util.DateUtils.toDisplayDate
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddReceipt: () -> Unit,
    onNavigateToReceiptDetail: (Long) -> Unit,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الوصولات (${state.receipts.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt, "فلترة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddReceipt,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("إضافة وصل") },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── Search Bar ──────────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("بحث برقم الوصل أو اسم المحل...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Clear, null) }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Filter Panel ────────────────────────────────────────
            AnimatedVisibility(visible = showFilters) {
                FilterPanel(
                    budgets = state.budgets,
                    selectedBudgetId = state.selectedBudgetId,
                    onBudgetSelected = viewModel::onBudgetFilterChange,
                    onClearFilters = { viewModel.clearFilters(); showFilters = false }
                )
            }

            // ── List ────────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.receipts.isEmpty()) {
                EmptyState("لا توجد وصولات")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.receipts, key = { it.id }) { receipt ->
                        ReceiptListItem(
                            receipt = receipt,
                            onClick = { onNavigateToReceiptDetail(receipt.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun FilterPanel(
    budgets: List<com.procurement.manager.domain.model.Budget>,
    selectedBudgetId: Long,
    onBudgetSelected: (Long) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("تصفية حسب الميزانية", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            // Budget chips
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedBudgetId == -1L,
                        onClick = { onBudgetSelected(-1L) },
                        label = { Text("الكل") }
                    )
                }
                items(budgets) { b ->
                    FilterChip(
                        selected = selectedBudgetId == b.id,
                        onClick = { onBudgetSelected(b.id) },
                        label = { Text(b.name) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onClearFilters) { Text("مسح الفلاتر") }
        }
    }
}

@Composable
fun ReceiptListItem(receipt: Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            if (receipt.imagePath != null) {
                AsyncImage(
                    model = File(receipt.imagePath),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
            } else {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(10.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(receipt.storeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("# ${receipt.receiptNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(2.dp))
                    Text(receipt.date.toDisplayDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (receipt.budgetName.isNotBlank()) {
                        Text(" • ${receipt.budgetName}", style = MaterialTheme.typography.bodySmall, color = SecondaryTeal)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(receipt.amount.formatCurrency(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                if (receipt.imagePath != null) {
                    Icon(Icons.Default.Image, null, Modifier.size(14.dp), tint = AccentAmber)
                }
            }
        }
    }
}
