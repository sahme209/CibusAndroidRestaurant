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
    data object Entry      : RestaurantRoute("entry")
    data object Login      : RestaurantRoute("login")
    data object Apply      : RestaurantRoute("apply")
    data object Onboarding : RestaurantRoute("onboarding")
    data object Wizard     : RestaurantRoute("wizard")
    data object NewPartner : RestaurantRoute("new_partner")
    data object Claim      : RestaurantRoute("claim/{restaurantId}/{restaurantName}")
    data object Documents  : RestaurantRoute("documents/{claimId}")
    data object Status     : RestaurantRoute("status")
    data object Main       : RestaurantRoute("main")
    data object OnboardingMenu : RestaurantRoute("onboarding_menu")
}

private fun navigateToEntry(navController: androidx.navigation.NavController) {
    navController.navigate(RestaurantRoute.Entry.route) {
        popUpTo(0) { inclusive = true }
    }
}

private data class RestaurantSessionState(
    val listingState: RestaurantListingState,
    val claimStatus: ClaimStatusSummary?,
    val isOperational: Boolean,
)

private suspend fun loadRestaurantSessionState(): RestaurantSessionState? {
    val store = RetrofitClient.getTokenStore()
    if (!store.hasValidToken()) return null

    val meResponse = try {
        RetrofitClient.restaurantApi.getMe()
    } catch (_: Exception) {
        return null
    }

    if (meResponse.code() == 401) {
        store.clear()
        return null
    }

    if (!meResponse.isSuccessful) return null
    val me = meResponse.body() ?: return null

    val appStatus = me.applicationStatus.orEmpty().lowercase()
    val isPendingReview = me.newVenuePendingReview == true ||
        me.menuSelfServeBlocked == true ||
        appStatus.contains("pending") ||
        appStatus.contains("submitted") ||
        appStatus.contains("review")

    val status = RetrofitClient.restaurantApi.fetchClaimStatus()
    if (status != null) {
        return RestaurantSessionState(
            listingState = status.state,
            claimStatus = status,
            isOperational = status.canOperate,
        )
    }

    val hasRestaurant = !me.restaurantId.isNullOrBlank()
    val isOperational = hasRestaurant && !isPendingReview
    return RestaurantSessionState(
        listingState = if (isOperational) {
            RestaurantListingState.VERIFIED_PARTNER
        } else {
            RestaurantListingState.CLAIM_SUBMITTED
        },
        claimStatus = null,
        isOperational = isOperational,
    )
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
                navigateToEntry(navController)
            }
        }
        onDispose { RestaurantSessionCallbacks.on401 = null }
    }

    LaunchedEffect(Unit) {
        val sessionState = loadRestaurantSessionState()
        if (sessionState != null) {
            isLoggedIn = true
            claimStatus = sessionState.claimStatus
            listingState = sessionState.listingState
            isOperational = sessionState.isOperational

            val dest = if (sessionState.isOperational) RestaurantRoute.Main.route else RestaurantRoute.Onboarding.route
            navController.navigate(dest) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = RestaurantRoute.Entry.route
    ) {
        composable(RestaurantRoute.Entry.route) {
            EntryScreen(
                onGetStarted = { navController.navigate(RestaurantRoute.NewPartner.route) },
                onSignIn = { navController.navigate(RestaurantRoute.Login.route) },
            )
        }

        composable(RestaurantRoute.Login.route) {
            LoginScreen(
                onBackToEntry = { navController.popBackStack() },
                onApplyClick = { navController.navigate(RestaurantRoute.Apply.route) },
                onRegisterClick = { navController.navigate(RestaurantRoute.NewPartner.route) },
                onLoginSuccess = {
                    val sessionState = loadRestaurantSessionState()
                    if (sessionState == null) {
                        "We could not load your restaurant account. Please try again."
                    } else {
                        isLoggedIn = true
                        claimStatus = sessionState.claimStatus
                        listingState = sessionState.listingState
                        isOperational = sessionState.isOperational

                        val dest = if (sessionState.isOperational) {
                            RestaurantRoute.Main.route
                        } else {
                            RestaurantRoute.Onboarding.route
                        }
                        navController.navigate(dest) {
                            popUpTo(RestaurantRoute.Entry.route) { inclusive = true }
                        }
                        null
                    }
                }
            )
        }

        composable(RestaurantRoute.Apply.route) {
            ApplyScreen(onBackToLogin = { navController.popBackStack() })
        }

        // Discovery-first partner flow (primary "Get Started" target)
        composable(RestaurantRoute.NewPartner.route) {
            NewPartnerFlowScreen(
                onCompleted = { token, _ ->
                    RetrofitClient.getTokenStore().saveToken(token)
                    isLoggedIn = true
                    isOperational = false
                    listingState = RestaurantListingState.CLAIM_SUBMITTED
                    navController.navigate(RestaurantRoute.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDismiss = { navController.popBackStack() },
                onManualRegister = {
                    navController.navigate(RestaurantRoute.Wizard.route) {
                        popUpTo(RestaurantRoute.NewPartner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RestaurantRoute.Wizard.route) {
            SimpleOnboardingWizard(
                onDismiss = { navController.popBackStack() },
                onCompleted = { token, _ ->
                    RetrofitClient.getTokenStore().saveToken(token)
                    isLoggedIn = true
                    isOperational = false
                    listingState = RestaurantListingState.CLAIM_SUBMITTED
                    navController.navigate(RestaurantRoute.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
                },
                onGetStarted = {
                    navController.navigate(RestaurantRoute.NewPartner.route)
                },
                onClaimRefreshed = { status ->
                    claimStatus = status
                    listingState = status.state
                    if (status.canOperate) {
                        isOperational = true
                        navController.navigate(RestaurantRoute.Main.route) {
                            popUpTo(RestaurantRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                },
                onViewMenu = {
                    navController.navigate(RestaurantRoute.OnboardingMenu.route)
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
                navController.navigate(RestaurantRoute.Entry.route) {
                    popUpTo(RestaurantRoute.Main.route) { inclusive = true }
                }
            })
        }

        composable(RestaurantRoute.OnboardingMenu.route) {
            RestaurantMenuContent()
        }
    }
}
