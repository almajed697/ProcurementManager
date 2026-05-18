package com.procurement.manager.util

import android.content.Context
import android.graphics.BitmapFactory
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.*
import com.procurement.manager.domain.model.Budget
import com.procurement.manager.domain.model.Receipt
import com.procurement.manager.util.CurrencyUtils.formatCurrency
import com.procurement.manager.util.DateUtils.toDisplayDate
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateReport(
        context: Context,
        receipts: List<Receipt>,
        budgets: List<Budget>,
        title: String = "تقرير المصاريف"
    ): File {
        val dir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "Report_$timestamp.pdf")

        val pdfDoc = PdfDocument(PdfWriter(file))
        val doc = Document(pdfDoc, PageSize.A4)
        doc.setMargins(36f, 36f, 36f, 36f)

        // Use a built-in font that supports Arabic (basic fallback)
        val font = PdfFontFactory.createFont()

        // Title
        val titlePara = Paragraph(title)
            .setFontSize(20f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(21, 101, 192))
            .setMarginBottom(8f)
        doc.add(titlePara)

        // Subtitle
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        doc.add(
            Paragraph("تاريخ الإنشاء: $dateStr")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(16f)
        )

        // ── Summary Table ────────────────────────────────────────
        val totalBudget = budgets.sumOf { it.totalAmount }
        val totalSpent  = receipts.sumOf { it.amount }
        val remaining   = totalBudget - totalSpent

        doc.add(Paragraph("ملخص عام").setBold().setFontSize(14f).setMarginBottom(4f))

        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
            .useAllAvailableWidth()
            .setMarginBottom(16f)

        addSummaryCell(summaryTable, "إجمالي الميزانية", totalBudget.formatCurrency())
        addSummaryCell(summaryTable, "المصروف",          totalSpent.formatCurrency())
        addSummaryCell(summaryTable, "المتبقي",          remaining.formatCurrency())
        doc.add(summaryTable)

        // ── Per-Budget Section ───────────────────────────────────
        doc.add(Paragraph("تفاصيل الميزانيات").setBold().setFontSize(14f).setMarginBottom(8f))

        for (budget in budgets) {
            val budgetReceipts = receipts.filter { it.budgetId == budget.id }
            val budgetSpent    = budgetReceipts.sumOf { it.amount }

            doc.add(
                Paragraph("• ${budget.name} — الميزانية: ${budget.totalAmount.formatCurrency()}  |  المصروف: ${budgetSpent.formatCurrency()}  |  المتبقي: ${(budget.totalAmount - budgetSpent).formatCurrency()}")
                    .setBold()
                    .setFontSize(11f)
                    .setFontColor(DeviceRgb(21, 101, 192))
                    .setMarginBottom(4f)
            )

            if (budgetReceipts.isNotEmpty()) {
                val table = Table(UnitValue.createPercentArray(floatArrayOf(1.5f, 2f, 1.2f, 1.5f, 2.5f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(12f)

                // Header
                listOf("رقم الوصل", "المحل", "التاريخ", "المبلغ", "ملاحظات").forEach { h ->
                    table.addHeaderCell(
                        Cell().add(Paragraph(h).setBold().setFontSize(9f))
                            .setBackgroundColor(DeviceRgb(214, 228, 255))
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                }

                for (r in budgetReceipts) {
                    table.addCell(Cell().add(Paragraph(r.receiptNumber).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Cell().add(Paragraph(r.storeName).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Cell().add(Paragraph(r.date.toDisplayDate()).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Cell().add(Paragraph(r.amount.formatCurrency()).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Cell().add(Paragraph(r.notes.ifBlank { "-" }).setFontSize(9f)))
                }
                doc.add(table)
            } else {
                doc.add(Paragraph("لا توجد وصولات لهذه الميزانية").setFontSize(10f).setItalic().setMarginBottom(8f))
            }
        }

        // ── All Receipts ─────────────────────────────────────────
        if (receipts.isNotEmpty()) {
            doc.add(Paragraph("جميع الوصولات (${receipts.size} وصل)").setBold().setFontSize(14f).setMarginTop(8f).setMarginBottom(4f))

            val fullTable = Table(UnitValue.createPercentArray(floatArrayOf(1.5f, 2f, 1.2f, 1.5f, 2f, 2f)))
                .useAllAvailableWidth()

            listOf("رقم الوصل", "المحل", "التاريخ", "المبلغ", "الميزانية", "ملاحظات").forEach { h ->
                fullTable.addHeaderCell(
                    Cell().add(Paragraph(h).setBold().setFontSize(9f))
                        .setBackgroundColor(DeviceRgb(214, 228, 255))
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }

            for (r in receipts) {
                fullTable.addCell(Cell().add(Paragraph(r.receiptNumber).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                fullTable.addCell(Cell().add(Paragraph(r.storeName).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                fullTable.addCell(Cell().add(Paragraph(r.date.toDisplayDate()).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                fullTable.addCell(Cell().add(Paragraph(r.amount.formatCurrency()).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                fullTable.addCell(Cell().add(Paragraph(r.budgetName).setFontSize(9f)).setTextAlignment(TextAlignment.CENTER))
                fullTable.addCell(Cell().add(Paragraph(r.notes.ifBlank { "-" }).setFontSize(9f)))
            }
            doc.add(fullTable)
        }

        doc.close()
        return file
    }

    private fun addSummaryCell(table: Table, label: String, value: String) {
        table.addCell(
            Cell().add(
                Paragraph("$label\n$value")
                    .setBold()
                    .setFontSize(11f)
                    .setTextAlignment(TextAlignment.CENTER)
            ).setBackgroundColor(DeviceRgb(232, 238, 244)).setPadding(8f)
        )
    }
}
