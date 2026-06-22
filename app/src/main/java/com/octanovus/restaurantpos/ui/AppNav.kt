package com.octanovus.restaurantpos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.octanovus.restaurantpos.ui.tables.TablesScreen
import com.octanovus.restaurantpos.ui.login.LoginScreen
import com.octanovus.restaurantpos.ui.order.OrderScreen
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
            OrderScreen(tableId = tableId, onBack = { nav.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
