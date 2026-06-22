package com.octanovus.restaurantpos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.octanovus.restaurantpos.ui.tables.TablesScreen
import com.octanovus.restaurantpos.ui.login.LoginScreen
import com.octanovus.restaurantpos.ui.order.OrderScreen
import com.octanovus.restaurantpos.ui.order.OrderViewModel
import com.octanovus.restaurantpos.ui.order.ViewOrderScreen
import com.octanovus.restaurantpos.ui.settings.SettingsScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "login") {

        composable("login") {
            LoginScreen(onLoggedIn = {
                nav.navigate("tables") { popUpTo("login") { inclusive = true } }
            })
        }

        composable("tables") {
            TablesScreen(
                onTableClick = { table -> nav.navigate("order/${table.id}") },
                onLogout = {
                    nav.navigate("login") { popUpTo("tables") { inclusive = true } }
                },
                onSettings = { nav.navigate("settings") }
            )
        }

        composable(
            "order/{tableId}",
            arguments = listOf(navArgument("tableId") { type = NavType.StringType })
        ) { backStack ->
            val tableId = backStack.arguments?.getString("tableId")!!
            OrderScreen(tableId = tableId, onBack = { nav.popBackStack() },  onViewOrder = { nav.navigate("vieworder/$tableId") })
        }

        composable(
            "vieworder/{tableId}",
            arguments = listOf(navArgument("tableId") { type = NavType.StringType })
        ) { backStack ->
            val tableId = backStack.arguments?.getString("tableId")!!
            // Share the exact OrderViewModel instance owned by the order/{tableId}
            // entry, so the cart is the same on both screens.
            val parentEntry = remember(backStack) { nav.getBackStackEntry("order/$tableId") }
            val vm: OrderViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = viewModelFactory { initializer { OrderViewModel(tableId) } }
            )
            ViewOrderScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onConfirmed = { nav.popBackStack("tables", inclusive = false) }
            )
        }


        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
