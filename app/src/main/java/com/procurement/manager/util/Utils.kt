package com.procurement.manager.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun Long.toDisplayDate(): String = displayFormat.format(Date(this))
    fun Long.toIsoDate(): String = isoFormat.format(Date(this))
    fun String.parseDisplayDate(): Long? = runCatching { displayFormat.parse(this)?.time }.getOrNull()
    fun currentTimestamp(): Long = System.currentTimeMillis()
    fun todayMillis(): Long {
        val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    fun endOfDayMillis(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
        return cal.timeInMillis
    }
}

object CurrencyUtils {
    fun Double.formatCurrency(): String {
        val format = NumberFormat.getNumberInstance(Locale("ar", "SA"))
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return "${format.format(this)} ر.س"
    }
    fun Double.formatAmount(): String {
        val format = NumberFormat.getNumberInstance(Locale.getDefault())
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        return format.format(this)
    }
}

object ImageUtils {
    fun createReceiptImageFile(context: Context, receiptNumber: String): File {
        val dir = File(context.filesDir, "receipts").also { it.mkdirs() }
        val timestamp = System.currentTimeMillis()
        return File(dir, "Receipt_${receiptNumber}_$timestamp.jpg")
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun deleteImageFile(imagePath: String) {
        runCatching { File(imagePath).delete() }
    }
}

object ValidationUtils {
    fun validateReceiptNumber(value: String): String? {
        return when {
            value.isBlank() -> "رقم الوصل مطلوب"
            value.length < 2 -> "رقم الوصل يجب أن يكون حرفين على الأقل"
            else -> null
        }
    }
    fun validateStoreName(value: String): String? {
        return when {
            value.isBlank() -> "اسم المحل مطلوب"
            else -> null
        }
    }
    fun validateAmount(value: String): String? {
        val amount = value.toDoubleOrNull()
        return when {
            value.isBlank() -> "المبلغ مطلوب"
            amount == null -> "يرجى إدخال مبلغ صحيح"
            amount <= 0 -> "يجب أن يكون المبلغ أكبر من صفر"
            else -> null
        }
    }
    fun validateBudgetName(value: String): String? {
        return when {
            value.isBlank() -> "اسم الميزانية مطلوب"
            else -> null
        }
    }
    fun validateBudgetAmount(value: String): String? {
        val amount = value.toDoubleOrNull()
        return when {
            value.isBlank() -> "مبلغ الميزانية مطلوب"
            amount == null -> "يرجى إدخال مبلغ صحيح"
            amount <= 0 -> "يجب أن يكون المبلغ أكبر من صفر"
            else -> null
        }
    }
}
