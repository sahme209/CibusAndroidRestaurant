package com.cibus.restaurant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cibus.restaurant.api.RetrofitClient

sealed class RestaurantRoute(val route: String) {
    data object Login : RestaurantRoute("login")
    data object Apply : RestaurantRoute("apply")
    data object Main : RestaurantRoute("main")
}

@Composable
fun RestaurantApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (RetrofitClient.getTokenStore().hasValidToken()) {
            isLoggedIn = true
            navController.navigate(RestaurantRoute.Main.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = RestaurantRoute.Login.route
    ) {
        composable(RestaurantRoute.Login.route) {
            LoginScreen(
                onApplyClick = { navController.navigate(RestaurantRoute.Apply.route) },
                onLoginSuccess = {
                    isLoggedIn = true
                    navController.navigate(RestaurantRoute.Main.route) {
                        popUpTo(RestaurantRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(RestaurantRoute.Apply.route) {
            ApplyScreen(onBackToLogin = { navController.popBackStack() })
        }
        composable(RestaurantRoute.Main.route) {
            RestaurantMainScreen(onLogout = {
                RetrofitClient.getTokenStore().clear()
                isLoggedIn = false
                navController.navigate(RestaurantRoute.Login.route) {
                    popUpTo(RestaurantRoute.Main.route) { inclusive = true }
                }
            })
        }
    }
}
