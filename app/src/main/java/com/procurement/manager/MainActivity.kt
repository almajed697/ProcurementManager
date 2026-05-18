package com.procurement.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.procurement.manager.presentation.navigation.ProcurementNavGraph
import com.procurement.manager.presentation.navigation.Screen
import com.procurement.manager.presentation.theme.ProcurementTheme
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProcurementTheme {
                ProcurementManagerApp()
            }
        }
    }
}

@Composable
fun ProcurementManagerApp() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("الرئيسية",   Icons.Default.Dashboard,   Screen.Dashboard.route),
        BottomNavItem("الميزانيات", Icons.Default.AccountBalance, Screen.BudgetList.route),
        BottomNavItem("الوصولات",   Icons.Default.Receipt,     Screen.ReceiptList.route),
        BottomNavItem("التقارير",   Icons.Default.BarChart,    Screen.Reports.route),
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            ProcurementNavGraph(navController = navController)
        }
    }
}
