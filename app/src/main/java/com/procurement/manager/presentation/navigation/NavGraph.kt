package com.procurement.manager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.procurement.manager.presentation.screens.budget.*
import com.procurement.manager.presentation.screens.dashboard.DashboardScreen
import com.procurement.manager.presentation.screens.receipt.*
import com.procurement.manager.presentation.screens.report.ReportsScreen

@Composable
fun ProcurementNavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        // ── Dashboard ────────────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToBudgets       = { navController.navigate(Screen.BudgetList.route) },
                onNavigateToReceipts      = { navController.navigate(Screen.ReceiptList.route) },
                onNavigateToAddReceipt    = { navController.navigate(Screen.AddEditReceipt.createRoute()) },
                onNavigateToReceiptDetail = { id -> navController.navigate(Screen.ReceiptDetail.createRoute(id)) }
            )
        }

        // ── Budget List ──────────────────────────────────────────────
        composable(Screen.BudgetList.route) {
            BudgetListScreen(
                onNavigateBack          = { navController.popBackStack() },
                onNavigateToAddBudget   = { navController.navigate(Screen.AddEditBudget.createRoute()) },
                onNavigateToEditBudget  = { id -> navController.navigate(Screen.AddEditBudget.createRoute(id)) }
            )
        }

        // ── Add / Edit Budget ────────────────────────────────────────
        composable(
            route = Screen.AddEditBudget.route,
            arguments = listOf(navArgument("budgetId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStack ->
            val budgetId = backStack.arguments?.getLong("budgetId")?.takeIf { it > 0 }
            AddEditBudgetScreen(
                budgetId        = budgetId,
                onNavigateBack  = { navController.popBackStack() }
            )
        }

        // ── Receipt List ─────────────────────────────────────────────
        composable(Screen.ReceiptList.route) {
            ReceiptListScreen(
                onNavigateBack             = { navController.popBackStack() },
                onNavigateToAddReceipt     = { navController.navigate(Screen.AddEditReceipt.createRoute()) },
                onNavigateToReceiptDetail  = { id -> navController.navigate(Screen.ReceiptDetail.createRoute(id)) }
            )
        }

        // ── Add / Edit Receipt ───────────────────────────────────────
        composable(
            route = Screen.AddEditReceipt.route,
            arguments = listOf(
                navArgument("receiptId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("budgetId")  { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStack ->
            val receiptId = backStack.arguments?.getLong("receiptId")?.takeIf { it > 0 }
            val budgetId  = backStack.arguments?.getLong("budgetId")?.takeIf { it > 0 }
            AddEditReceiptScreen(
                receiptId              = receiptId,
                preselectedBudgetId    = budgetId,
                onNavigateBack         = { navController.popBackStack() },
                onNavigateToCamera     = { navController.navigate(Screen.Camera.createRoute()) }
            )
        }

        // ── Receipt Detail ───────────────────────────────────────────
        composable(
            route = Screen.ReceiptDetail.route,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
        ) { backStack ->
            val receiptId = backStack.arguments!!.getLong("receiptId")
            ReceiptDetailScreen(
                receiptId               = receiptId,
                onNavigateBack          = { navController.popBackStack() },
                onNavigateToEdit        = { id -> navController.navigate(Screen.AddEditReceipt.createRoute(receiptId = id)) },
                onNavigateToImageViewer = { path ->
                    navController.navigate(Screen.ImageViewer.createRoute(path))
                }
            )
        }

        // ── Camera ───────────────────────────────────────────────────
        composable(
            route = Screen.Camera.route,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            CameraScreen(
                receiptNumber   = "receipt",
                onImageCaptured = { _ -> navController.popBackStack() },
                onNavigateBack  = { navController.popBackStack() }
            )
        }

        // ── Image Viewer ─────────────────────────────────────────────
        composable(
            route = Screen.ImageViewer.route,
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { backStack ->
            val imagePath = backStack.arguments!!.getString("imagePath") ?: ""
            ImageViewerScreen(
                imagePath       = imagePath,
                onNavigateBack  = { navController.popBackStack() }
            )
        }

        // ── Reports ──────────────────────────────────────────────────
        composable(Screen.Reports.route) {
            ReportsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
