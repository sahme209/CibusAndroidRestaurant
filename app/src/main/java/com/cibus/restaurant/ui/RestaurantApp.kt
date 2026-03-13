package com.cibus.restaurant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class RestaurantRoute(val route: String) {
    data object Login : RestaurantRoute("login")
    data object Apply : RestaurantRoute("apply")
    data object Main : RestaurantRoute("main")
}

@Composable
fun RestaurantApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RestaurantRoute.Login.route
    ) {
        composable(RestaurantRoute.Login.route) {
            LoginScreen(
                onApplyClick = { navController.navigate(RestaurantRoute.Apply.route) },
                onLoginSuccess = {
                    navController.navigate(RestaurantRoute.Main.route) {
                        popUpTo(RestaurantRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(RestaurantRoute.Apply.route) {
            ApplyScreen()
        }
        composable(RestaurantRoute.Main.route) {
            RestaurantMainScreen()
        }
    }
}
