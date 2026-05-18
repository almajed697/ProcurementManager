package com.procurement.manager.presentation.screens.receipt

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.presentation.components.ConfirmDeleteDialog
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency
import com.procurement.manager.util.DateUtils.toDisplayDate
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    receiptId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToImageViewer: (String) -> Unit,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val receipt by viewModel.getReceiptById(receiptId).collectAsStateWithLifecycle(initialValue = null)
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(300)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل الوصل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                actions = {
                    receipt?.let { r ->
                        IconButton(onClick = { onNavigateToEdit(r.id) }) {
                            Icon(Icons.Default.Edit, "تعديل")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "حذف", tint = ErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ) { padding ->
        receipt?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Image ────────────────────────────────────────────
                r.imagePath?.let { path ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToImageViewer(path) }
                    ) {
                        Box {
                            AsyncImage(
                                model = File(path),
                                contentDescription = "صورة الوصل",
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                contentScale = ContentScale.Crop
                            )
                            // Zoom hint
                            Icon(
                                Icons.Default.ZoomIn,
                                null,
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                // ── Main Info Card ───────────────────────────────────
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(3.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow(Icons.Default.Tag, "رقم الوصل", r.receiptNumber)
                        HorizontalDivider()
                        DetailRow(Icons.Default.Store, "اسم المحل", r.storeName)
                        HorizontalDivider()
                        DetailRow(Icons.Default.CalendarToday, "التاريخ", r.date.toDisplayDate())
                        HorizontalDivider()
                        DetailRow(Icons.Default.AttachMoney, "المبلغ", r.amount.formatCurrency(), valueColor = PrimaryBlue)
                        HorizontalDivider()
                        DetailRow(Icons.Default.AccountBalance, "الميزانية", r.budgetName)
                        if (r.notes.isNotBlank()) {
                            HorizontalDivider()
                            DetailRow(Icons.Default.Notes, "ملاحظات", r.notes)
                        }
                    }
                }

                // ── Metadata ─────────────────────────────────────────
                Text(
                    "أُضيف في: ${r.createdAt.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } ?: Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            message = "هل تريد حذف هذا الوصل؟",
            onConfirm = { receipt?.let { viewModel.deleteReceipt(it) }; showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = PrimaryBlue)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
        }
    }
}
