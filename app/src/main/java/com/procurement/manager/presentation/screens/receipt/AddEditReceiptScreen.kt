package com.procurement.manager.presentation.screens.receipt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.procurement.manager.presentation.components.*
import com.procurement.manager.presentation.theme.*
import com.procurement.manager.util.DateUtils
import com.procurement.manager.util.DateUtils.toDisplayDate
import com.procurement.manager.util.ImageUtils
import com.procurement.manager.util.ValidationUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEditReceiptScreen(
    receiptId: Long?,
    preselectedBudgetId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: ReceiptViewModel = hiltViewModel()
) {
    val context     = LocalContext.current
    val state       by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    // Load receipt for edit
    val existingReceipt by (receiptId?.let { viewModel.getReceiptById(it) } ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)

    // Form state
    var receiptNumber by remember { mutableStateOf("") }
    var storeName     by remember { mutableStateOf("") }
    var dateDisplay   by remember { mutableStateOf(DateUtils.todayMillis().toDisplayDate()) }
    var dateMillis    by remember { mutableLongStateOf(DateUtils.todayMillis()) }
    var amount        by remember { mutableStateOf("") }
    var notes         by remember { mutableStateOf("") }
    var selectedBudgetId by remember { mutableLongStateOf(-1L) }
    var imagePath     by remember { mutableStateOf<String?>(null) }
    var tempImageUri  by remember { mutableStateOf<Uri?>(null) }

    // Errors
    var receiptNumError by remember { mutableStateOf<String?>(null) }
    var storeNameError  by remember { mutableStateOf<String?>(null) }
    var amountError     by remember { mutableStateOf<String?>(null) }
    var budgetError     by remember { mutableStateOf<String?>(null) }

    var budgetMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker     by remember { mutableStateOf(false) }

    // Pre-fill from existing receipt
    LaunchedEffect(existingReceipt) {
        existingReceipt?.let { r ->
            receiptNumber    = r.receiptNumber
            storeName        = r.storeName
            dateMillis       = r.date
            dateDisplay      = r.date.toDisplayDate()
            amount           = r.amount.toString()
            notes            = r.notes
            selectedBudgetId = r.budgetId
            imagePath        = r.imagePath
        }
    }

    // Pre-fill budget
    LaunchedEffect(preselectedBudgetId, state.budgets) {
        if (selectedBudgetId == -1L && preselectedBudgetId != null && preselectedBudgetId > 0) {
            selectedBudgetId = preselectedBudgetId
        } else if (selectedBudgetId == -1L && state.budgets.isNotEmpty()) {
            selectedBudgetId = state.budgets.first().id
        }
    }

    // Apply OCR result
    LaunchedEffect(state.ocrResult) {
        state.ocrResult?.let { ocr ->
            if (ocr.receiptNumber != null && receiptNumber.isBlank()) receiptNumber = ocr.receiptNumber
            if (ocr.storeName != null && storeName.isBlank()) storeName = ocr.storeName
            if (ocr.amount != null && amount.isBlank()) amount = ocr.amount.toString()
            if (ocr.date != null) { dateMillis = ocr.date; dateDisplay = ocr.date.toDisplayDate() }
        }
    }

    // Navigate back after save
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(500)
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val savedPath = viewModel.saveImageToLocal(context, it, receiptNumber.ifBlank { "temp" })
            imagePath = savedPath
            viewModel.processOcr(context, it)
        }
    }

    // Camera URI
    val cameraFile = remember { mutableStateOf<File?>(null) }
    val cameraUri  = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri.value?.let { uri ->
                val savedPath = viewModel.saveImageToLocal(context, uri, receiptNumber.ifBlank { "temp" })
                imagePath = savedPath
                viewModel.processOcr(context, uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (receiptId != null) "تعديل الوصل" else "إضافة وصل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Image Section ────────────────────────────────────
                ImageSection(
                    imagePath = imagePath,
                    isOcrProcessing = state.isOcrProcessing,
                    onTakePhoto = {
                        if (cameraPermission.status.isGranted) {
                            val file = ImageUtils.createReceiptImageFile(context, receiptNumber.ifBlank { "temp" })
                            cameraFile.value = file
                            val uri = ImageUtils.getUriForFile(context, file)
                            cameraUri.value = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                    onChooseGallery = { galleryLauncher.launch("image/*") },
                    onRemoveImage = { imagePath = null }
                )

                HorizontalDivider()

                // ── Form Fields ──────────────────────────────────────
                FormTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it; receiptNumError = null },
                    label = "رقم الوصل *",
                    isError = receiptNumError != null,
                    errorMessage = receiptNumError,
                    leadingIcon = { Icon(Icons.Default.Tag, null) }
                )

                FormTextField(
                    value = storeName,
                    onValueChange = { storeName = it; storeNameError = null },
                    label = "اسم المحل *",
                    isError = storeNameError != null,
                    errorMessage = storeNameError,
                    leadingIcon = { Icon(Icons.Default.Store, null) }
                )

                // Date field
                OutlinedTextField(
                    value = dateDisplay,
                    onValueChange = {},
                    label = { Text("التاريخ *") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.EditCalendar, "اختيار تاريخ")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                FormTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = null },
                    label = "المبلغ (ر.س) *",
                    isError = amountError != null,
                    errorMessage = amountError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) }
                )

                // Budget Dropdown
                Column {
                    ExposedDropdownMenuBox(expanded = budgetMenuExpanded, onExpandedChange = { budgetMenuExpanded = it }) {
                        OutlinedTextField(
                            value = state.budgets.find { it.id == selectedBudgetId }?.name ?: "اختر الميزانية",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الميزانية *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = budgetMenuExpanded) },
                            isError = budgetError != null,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, null) }
                        )
                        ExposedDropdownMenu(expanded = budgetMenuExpanded, onDismissRequest = { budgetMenuExpanded = false }) {
                            state.budgets.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.name) },
                                    onClick = { selectedBudgetId = b.id; budgetMenuExpanded = false; budgetError = null }
                                )
                            }
                        }
                    }
                    if (budgetError != null) {
                        Text(budgetError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                    }
                }

                FormTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "ملاحظات (اختياري)",
                    singleLine = false,
                    maxLines = 3,
                    leadingIcon = { Icon(Icons.Default.Notes, null) }
                )

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // ── Save Button ──────────────────────────────────────
                Button(
                    onClick = {
                        receiptNumError = ValidationUtils.validateReceiptNumber(receiptNumber)
                        storeNameError  = ValidationUtils.validateStoreName(storeName)
                        amountError     = ValidationUtils.validateAmount(amount)
                        if (selectedBudgetId == -1L) budgetError = "يرجى اختيار الميزانية"

                        if (receiptNumError == null && storeNameError == null && amountError == null && budgetError == null) {
                            viewModel.saveReceipt(
                                id = receiptId,
                                receiptNumber = receiptNumber.trim(),
                                storeName     = storeName.trim(),
                                date          = dateMillis,
                                amount        = amount.toDouble(),
                                notes         = notes.trim(),
                                budgetId      = selectedBudgetId,
                                imagePath     = imagePath
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("حفظ الوصل", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(32.dp))
            }

            if (state.isOcrProcessing) { LoadingOverlay() }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateMillis = it; dateDisplay = it.toDisplayDate()
                    }
                    showDatePicker = false
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("إلغاء") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun ImageSection(
    imagePath: String?,
    isOcrProcessing: Boolean,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemoveImage: () -> Unit
) {
    Column {
        Text("صورة الوصل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (imagePath != null) {
            Box {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "صورة الوصل",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Cancel, "إزالة الصورة", tint = ErrorRed)
                }
                if (isOcrProcessing) {
                    Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("التقاط صورة")
            }
            OutlinedButton(
                onClick = onChooseGallery,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("اختيار صورة")
            }
        }
    }
}
