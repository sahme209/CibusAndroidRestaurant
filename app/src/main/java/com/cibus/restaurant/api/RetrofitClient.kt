package com.cibus.restaurant.api

import android.content.Context
import com.cibus.restaurant.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var tokenStore: RestaurantTokenStore? = null

    fun init(context: Context) {
        if (tokenStore == null) {
            tokenStore = RestaurantTokenStore(context.applicationContext)
        }
    }

    fun getTokenStore(): RestaurantTokenStore {
        return tokenStore ?: error("RetrofitClient not initialized - call RetrofitClient.init(context)")
    }

    private fun buildOkHttp(): OkHttpClient {
        val store = tokenStore
        return if (store == null) {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        } else {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(RestaurantAuthInterceptor(store))
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        }
    }

    private val baseUrl = BuildConfig.BASE_URL.trimEnd('/') + "/"

    val restaurantApi: RestaurantApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(buildOkHttp())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestaurantApi::class.java)
    }
}

// ── Claim / Verification convenience extensions ───────────────────────────────

/** Fetches the current claim/verification status. Returns null if not yet claimed or error. */
suspend fun RestaurantApi.fetchClaimStatus(): com.cibus.restaurant.claim.ClaimStatusSummary? {
    return try {
        val resp = getClaimStatus()
        if (!resp.isSuccessful) return null
        val body = resp.body() ?: return null
        val state = com.cibus.restaurant.claim.RestaurantListingState.from(body.state)
        com.cibus.restaurant.claim.ClaimStatusSummary(
            state = state,
            claimId = body.claimId,
            reviewNote = body.reviewNote
        )
    } catch (_: Exception) { null }
}

/** Submits a claim request and returns the generated claimId. */
suspend fun RestaurantApi.submitClaim(
    request: com.cibus.restaurant.claim.RestaurantClaimRequest
): okhttp3.ResponseBody? {
    val dto = RestaurantClaimApiRequest(
        restaurantId = request.restaurantId,
        restaurantName = request.restaurantName,
        ownerName = request.ownerName,
        role = request.role,
        email = request.email,
        phone = request.phone,
        cnic = request.cnic,
        businessName = request.businessName,
        ntnNumber = request.ntnNumber,
        pfaLicenseNumber = request.pfaLicenseNumber,
        notes = request.notes,
        confirmedAddress = request.confirmedAddress,
        payoutInfo = request.payoutInfo?.let { p ->
            PayoutInfoDto(
                accountTitle = p.accountTitle,
                bankName = p.bankName,
                iban = p.iban,
                jazzCashWallet = p.jazzCashWallet,
                easypaisaWallet = p.easypaisaWallet
            )
        }
    )
    val resp = submitClaim(dto)
    return resp.body()
}

/** Acknowledges documents submitted for a claim. Non-fatal. */
suspend fun RestaurantApi.acknowledgeDocuments(claimId: String) {
    try {
        acknowledgeDocuments(claimId, mapOf("acknowledged" to true))
    } catch (_: Exception) { }
}
