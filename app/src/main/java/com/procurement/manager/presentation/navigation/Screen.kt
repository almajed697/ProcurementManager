package com.procurement.manager.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object BudgetList : Screen("budget_list")
    object AddEditBudget : Screen("add_edit_budget?budgetId={budgetId}") {
        fun createRoute(budgetId: Long? = null) =
            if (budgetId != null) "add_edit_budget?budgetId=$budgetId" else "add_edit_budget"
    }
    object ReceiptList : Screen("receipt_list")
    object AddEditReceipt : Screen("add_edit_receipt?receiptId={receiptId}&budgetId={budgetId}") {
        fun createRoute(receiptId: Long? = null, budgetId: Long? = null): String {
            val params = buildString {
                if (receiptId != null) append("receiptId=$receiptId")
                if (budgetId != null) {
                    if (isNotEmpty()) append("&")
                    append("budgetId=$budgetId")
                }
            }
            return if (params.isEmpty()) "add_edit_receipt" else "add_edit_receipt?$params"
        }
    }
    object ReceiptDetail : Screen("receipt_detail/{receiptId}") {
        fun createRoute(receiptId: Long) = "receipt_detail/$receiptId"
    }
    object Camera : Screen("camera?receiptId={receiptId}") {
        fun createRoute(receiptId: Long? = null) =
            if (receiptId != null) "camera?receiptId=$receiptId" else "camera"
    }
    object ImageViewer : Screen("image_viewer?imagePath={imagePath}") {
        fun createRoute(imagePath: String) = "image_viewer?imagePath=${imagePath}"
    }
    object Reports : Screen("reports")
}
