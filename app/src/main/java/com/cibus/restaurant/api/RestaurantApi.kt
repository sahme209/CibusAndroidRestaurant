package com.cibus.restaurant.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
)

interface RestaurantApi {
    @POST("restaurant/auth/sign-in")
    suspend fun signIn(@Body request: RestaurantSignInRequest): Response<CibusAuthResponse>

    @POST("restaurant/auth/apply")
    suspend fun apply(@Body request: RestaurantApplyRequest): Response<CibusApplyResponse>

    @GET("restaurant/me")
    suspend fun getMe(): Response<RestaurantMeResponse>
}
