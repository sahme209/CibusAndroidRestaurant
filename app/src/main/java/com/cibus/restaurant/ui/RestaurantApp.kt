package com.cibus.restaurant.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cibus.restaurant.api.RestaurantSessionCallbacks
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.api.fetchClaimStatus
import com.cibus.restaurant.claim.ClaimStatusSummary
import com.cibus.restaurant.claim.RestaurantListingState
import com.cibus.restaurant.ui.claim.*

sealed class RestaurantRoute(val route: String) {
    data object Login      : RestaurantRoute("login")
    data object Apply      : RestaurantRoute("apply")
    data object Onboarding : RestaurantRoute("onboarding")
    data object Claim      : RestaurantRoute("claim/{restaurantId}/{restaurantName}")
    data object Documents  : RestaurantRoute("documents/{claimId}")
    data object Status     : RestaurantRoute("status")
    data object Main       : RestaurantRoute("main")
}

private fun navigateToLogin(navController: androidx.navigation.NavController) {
    navController.navigate(RestaurantRoute.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

@Composable
fun RestaurantApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    var listingState by remember { mutableStateOf(RestaurantListingState.UNCLAIMED) }
    var claimStatus by remember { mutableStateOf<ClaimStatusSummary?>(null) }
    var isOperational by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        RestaurantSessionCallbacks.on401 = {
            Handler(Looper.getMainLooper()).post {
                isLoggedIn = false
                navigateToLogin(navController)
            }
        }
        onDispose { RestaurantSessionCallbacks.on401 = null }
    }

    LaunchedEffect(Unit) {
        val store = RetrofitClient.getTokenStore()
        if (store.hasValidToken()) {
            var proceedToMain = true
            try {
                val resp = RetrofitClient.restaurantApi.getMe()
                if (resp.code() == 401) proceedToMain = false
            } catch (_: Exception) { }

            if (proceedToMain) {
                isLoggedIn = true
                // Fetch claim/verification status
                try {
                    val status = RetrofitClient.restaurantApi.fetchClaimStatus()
                    if (status != null) {
                        claimStatus = status
                        listingState = status.state
                        isOperational = status.canOperate
                    }
                } catch (_: Exception) { }

                val dest = if (isOperational) RestaurantRoute.Main.route else RestaurantRoute.Onboarding.route
                navController.navigate(dest) { popUpTo(0) { inclusive = true } }
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
                    navController.navigate(RestaurantRoute.Onboarding.route) {
                        popUpTo(RestaurantRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RestaurantRoute.Apply.route) {
            ApplyScreen(onBackToLogin = { navController.popBackStack() })
        }

        composable(RestaurantRoute.Onboarding.route) {
            PartnerOnboardingScreen(
                listingState = listingState,
                claimStatus = claimStatus,
                onClaimNavigate = { rId, rName ->
                    navController.navigate("claim/${rId}/${rName}")
                },
                onStatusNavigate = {
                    navController.navigate(RestaurantRoute.Status.route)
                },
                onVerified = {
                    isOperational = true
                    navController.navigate(RestaurantRoute.Main.route) {
                        popUpTo(RestaurantRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable("claim/{restaurantId}/{restaurantName}") { backStackEntry ->
            val rId = backStackEntry.arguments?.getString("restaurantId") ?: ""
            val rName = backStackEntry.arguments?.getString("restaurantName") ?: ""
            ClaimRestaurantScreen(
                restaurantId = rId,
                restaurantName = rName,
                onClaimSubmitted = { claimId ->
                    listingState = RestaurantListingState.CLAIM_SUBMITTED
                    navController.navigate("documents/$claimId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("documents/{claimId}") { backStackEntry ->
            val claimId = backStackEntry.arguments?.getString("claimId") ?: ""
            DocumentUploadScreen(
                claimId = claimId,
                onFinished = {
                    navController.navigate(RestaurantRoute.Status.route) {
                        popUpTo(RestaurantRoute.Onboarding.route)
                    }
                }
            )
        }

        composable(RestaurantRoute.Status.route) {
            VerificationStatusScreen(
                onVerified = {
                    isOperational = true
                    listingState = RestaurantListingState.VERIFIED_PARTNER
                    navController.navigate(RestaurantRoute.Main.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(RestaurantRoute.Main.route) {
            RestaurantMainScreen(onLogout = {
                RetrofitClient.getTokenStore().clear()
                isLoggedIn = false
                isOperational = false
                listingState = RestaurantListingState.UNCLAIMED
                navController.navigate(RestaurantRoute.Login.route) {
                    popUpTo(RestaurantRoute.Main.route) { inclusive = true }
                }
            })
        }
    }
}
