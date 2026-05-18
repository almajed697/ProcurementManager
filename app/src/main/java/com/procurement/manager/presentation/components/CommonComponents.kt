package com.procurement.manager.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.CurrencyUtils.formatCurrency

// ── Stat Card ─────────────────────────────────────────────────────────────────
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
    }
}

// ── Budget Progress Bar ────────────────────────────────────────────────────────
@Composable
fun BudgetProgressBar(
    spent: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
    val color = when {
        progress >= 0.9f -> ErrorRed
        progress >= 0.7f -> WarningOrange
        else             -> SuccessGreen
    }
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "progress"
    )
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(spent.formatCurrency(), style = MaterialTheme.typography.bodySmall, color = color)
            Text(total.formatCurrency(), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${(progress * 100).toInt()}% مُصرف",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

// ── Section Header ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

// ── Confirm Delete Dialog ──────────────────────────────────────────────────────
@Composable
fun ConfirmDeleteDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
        title = { Text("تأكيد الحذف") },
        text  = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("حذف", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// ── Empty State ────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Loading indicator ─────────────────────────────────────────────────────────
@Composable
fun LoadingOverlay() {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ── Form Text Field ────────────────────────────────────────────────────────────
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default
) {
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
