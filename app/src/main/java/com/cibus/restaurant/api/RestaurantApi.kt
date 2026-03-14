package com.cibus.restaurant.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

data class RestaurantSignInRequest(val email: String, val password: String)
data class RestaurantApplyRequest(
    @SerializedName("partnerName") val partnerName: String,
    val email: String,
    val password: String,
    @SerializedName("restaurantName") val restaurantName: String,
    val address: String,
    val city: String,
    val phone: String,
    val cnic: String,
    @SerializedName("ntnNumber") val ntnNumber: String,
    @SerializedName("pfaLicenseNumber") val pfaLicenseNumber: String,
)

data class CibusAuthData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int? = 86400,
)
data class CibusAuthResponse(val success: Boolean? = true, val data: CibusAuthData? = null)
data class CibusApplyResponse(val success: Boolean? = true, val message: String? = null)

data class RestaurantMeResponse(
    @SerializedName("partnerId") val partnerId: String? = null,
    @SerializedName("restaurantId") val restaurantId: String? = null,
    @SerializedName("restaurantName") val restaurantName: String? = null,
    @SerializedName("partnerName") val partnerName: String? = null,
    val email: String? = null,
    @SerializedName("applicationStatus") val applicationStatus: String? = "approved",
    @SerializedName("chainId") val chainId: String? = null,
    @SerializedName("chainName") val chainName: String? = null,
)

data class ChainMeResponse(
    val id: String,
    val name: String,
    val branches: List<ChainBranch>,
)
data class ChainBranch(
    val id: String,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val phone: String? = null,
)

data class ChainAnalyticsResponse(
    val chainId: String,
    @SerializedName("todayOrders") val todayOrders: Int = 0,
    @SerializedName("todayRevenue") val todayRevenue: Double = 0.0,
    val branches: Map<String, BranchMetrics> = emptyMap(),
    @SerializedName("branchIds") val branchIds: List<String> = emptyList(),
)
data class BranchMetrics(val orders: Int = 0, val revenue: Double = 0.0, val name: String = "")

interface RestaurantApi {
    @POST("restaurant/auth/sign-in")
    suspend fun signIn(@Body request: RestaurantSignInRequest): Response<CibusAuthResponse>

    @POST("restaurant/auth/apply")
    suspend fun apply(@Body request: RestaurantApplyRequest): Response<CibusApplyResponse>

    @GET("restaurant/me")
    suspend fun getMe(): Response<RestaurantMeResponse>

    @GET("chains/me")
    suspend fun getChainsMe(): Response<ChainMeResponse>

    @GET("chains/{id}/analytics")
    suspend fun getChainAnalytics(@Path("id") chainId: String): Response<ChainAnalyticsResponse>

    /** Phase 99: Marketplace signals — restaurant boosts. */
    @GET("marketplace-signals")
    suspend fun getMarketplaceSignals(@Query("restaurantId") restaurantId: String? = null): Response<MarketplaceSignalsRestaurantResponse>

    /** Restaurant orders. Backend: GET /restaurants/:id/orders */
    @GET("restaurants/{id}/orders")
    suspend fun getOrders(@Path("id") restaurantId: String, @Query("limit") limit: Int = 50): Response<List<RestaurantOrderDto>>

    @POST("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: String): Response<Unit>

    @POST("orders/{id}/reject")
    suspend fun rejectOrder(@Path("id") orderId: String): Response<Unit>

    @GET("restaurants/{id}/menu")
    suspend fun getMenu(@Path("id") restaurantId: String): Response<RestaurantMenuResponse>

    @PATCH("restaurants/{id}/menu")
    suspend fun patchMenu(@Path("id") restaurantId: String, @Body body: Map<String, Any>): Response<Unit>

    @PATCH("restaurants/{id}/availability")
    suspend fun patchAvailability(@Path("id") restaurantId: String, @Body body: Map<String, String>): Response<Unit>

    @PATCH("orders/{id}/status")
    suspend fun patchOrderStatus(@Path("id") orderId: String, @Body body: Map<String, Any>): Response<Unit>

    // ── Claim / Verification ──────────────────────────────────────────────────

    /** POST /restaurant-claims — submit a new claim for a public listing. */
    @POST("restaurant-claims")
    suspend fun submitClaim(@Body request: RestaurantClaimApiRequest): Response<okhttp3.ResponseBody>

    /** POST /restaurant-claims/{id}/documents — acknowledge documents submitted. */
    @POST("restaurant-claims/{id}/documents")
    suspend fun acknowledgeDocuments(
        @Path("id") claimId: String,
        @Body body: Map<String, Any> = mapOf("acknowledged" to true)
    ): Response<Unit>

    /** GET /restaurant-claims/status — get verification status for current restaurant. */
    @GET("restaurant-claims/status")
    suspend fun getClaimStatus(): Response<ClaimStatusApiResponse>
}
data class RestaurantMenuResponse(
    val categories: List<Map<String, Any>> = emptyList(),
    val menuStatus: String = "pending_partner_onboarding",
)
data class MarketplaceSignalsRestaurantResponse(
    val dynamicPromotions: List<Any> = emptyList(),
    val riderIncentives: List<Any> = emptyList(),
    val restaurantBoosts: List<RestaurantBoostDto> = emptyList()
)
data class RestaurantBoostDto(val restaurantId: String = "", val boostUntil: Long? = null)

data class RestaurantOrderDto(
    val id: String = "",
    val status: String? = null,
    val total: Double? = null,
    @SerializedName("restaurantName") val restaurantName: String? = null,
    val address: Map<String, Any>? = null,
    val items: List<Map<String, Any>>? = null,
)

// ── Claim DTOs ────────────────────────────────────────────────────────────────

data class RestaurantClaimApiRequest(
    @SerializedName("restaurantId")   val restaurantId: String,
    @SerializedName("restaurantName") val restaurantName: String,
    @SerializedName("ownerName")      val ownerName: String,
    val role: String,
    val email: String,
    val phone: String,
    val cnic: String,
    @SerializedName("businessName")      val businessName: String?,
    @SerializedName("ntnNumber")         val ntnNumber: String?,
    @SerializedName("pfaLicenseNumber")  val pfaLicenseNumber: String?,
    val notes: String?,
    @SerializedName("confirmedAddress")  val confirmedAddress: String,
    @SerializedName("payoutInfo")        val payoutInfo: PayoutInfoDto? = null
)

data class PayoutInfoDto(
    @SerializedName("accountTitle")  val accountTitle: String,
    @SerializedName("bankName")      val bankName: String,
    val iban: String,
    @SerializedName("jazzCashWallet") val jazzCashWallet: String? = null,
    @SerializedName("easypaisaWallet") val easypaisaWallet: String? = null
)

data class ClaimStatusApiResponse(
    val state: String? = null,
    @SerializedName("claimId") val claimId: String? = null,
    @SerializedName("reviewNote") val reviewNote: String? = null
)

