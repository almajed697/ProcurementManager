package com.procurement.manager.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.procurement.manager.domain.model.OcrResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromUri(uri: Uri): OcrResult {
        return suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val rawText = visionText.text
                        cont.resume(parseReceiptText(rawText))
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }
    }

    private fun parseReceiptText(text: String): OcrResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Extract amount - look for patterns like 1,234.56 or 1234.56 or ١٢٣٤
        val amountPattern = Pattern.compile("""(?:total|مجموع|إجمالي|المبلغ|الإجمالي)[:\s]*([0-9,،.٠-٩]+)""", Pattern.CASE_INSENSITIVE)
        val genericAmountPattern = Pattern.compile("""([0-9]{1,4}[,،]?[0-9]{3}(?:\.[0-9]{2})?|\d+\.\d{2})""")

        // Extract receipt number
        val receiptNumPattern = Pattern.compile("""(?:receipt|وصل|فاتورة|رقم)[#:\s]*([A-Za-z0-9\-]+)""", Pattern.CASE_INSENSITIVE)

        // Extract date patterns
        val datePatterns = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        )

        var receiptNumber: String? = null
        var amount: Double? = null
        var date: Long? = null
        var storeName: String? = null

        // Try to extract receipt number
        for (line in lines) {
            val matcher = receiptNumPattern.matcher(line)
            if (matcher.find()) {
                receiptNumber = matcher.group(1)
                break
            }
        }

        // Try to extract amount
        for (line in lines) {
            val matcher = amountPattern.matcher(line)
            if (matcher.find()) {
                amount = matcher.group(1)
                    ?.replace(",", "")
                    ?.replace("،", "")
                    ?.toDoubleOrNull()
                if (amount != null) break
            }
        }
        if (amount == null) {
            val amounts = mutableListOf<Double>()
            for (line in lines) {
                val matcher = genericAmountPattern.matcher(line)
                while (matcher.find()) {
                    matcher.group(1)?.replace(",", "")?.toDoubleOrNull()?.let { amounts.add(it) }
                }
            }
            amount = amounts.maxOrNull()
        }

        // Try to extract date
        val dateRegex = Regex("""\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{4}[/-]\d{2}[/-]\d{2}""")
        for (line in lines) {
            val dateStr = dateRegex.find(line)?.value
            if (dateStr != null) {
                for (fmt in datePatterns) {
                    date = runCatching { fmt.parse(dateStr)?.time }.getOrNull()
                    if (date != null) break
                }
                if (date != null) break
            }
        }

        // Store name is often the first non-empty line
        storeName = lines.firstOrNull()?.takeIf { it.length > 2 && it.length < 60 }

        return OcrResult(
            receiptNumber = receiptNumber,
            storeName = storeName,
            amount = amount,
            date = date,
            rawText = text
        )
    }
}
