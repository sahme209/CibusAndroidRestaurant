package com.cibus.restaurant.api

import android.content.Context
import com.cibus.restaurant.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/** Forces every request to bypass HTTP cache (prevents 304/ETag stale-data issues). */
private val noCacheInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .header("Cache-Control", "no-cache, no-store")
        .header("Pragma", "no-cache")
        .build()
    chain.proceed(request)
}

/**
 * Retry interceptor for transient connection errors (matches iOS resilientData).
 * Retries once after 300ms on connection reset / socket timeout — common with
 * stale HTTP/2 connections when the user spends time filling out forms.
 */
private val resilientRetryInterceptor = Interceptor { chain ->
    val request = chain.request()
    try {
        chain.proceed(request)
    } catch (e: java.io.IOException) {
        // Retry once on transient connection errors
        Thread.sleep(300)
        chain.proceed(request)
    }
}

/**
 * Deserializes timestamp fields that may arrive as either:
 *   - An ISO-8601 string: "2026-03-29T10:00:00.000Z"
 *   - A Firestore Timestamp object: { "_seconds": 1711706400, "_nanoseconds": 0 }
 *
 * Returns the value as an ISO-8601 string (or null on failure), matching what the
 * RestaurantOrderDto fields (preparingAt, createdAt, riderArrivedAt) expect.
 */
private val firestoreTimestampDeserializer = object : JsonDeserializer<String?> {
    private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).also {
        it.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): String? {
        if (json == null || json.isJsonNull) return null
        // Plain ISO string — pass through
        if (json.isJsonPrimitive) return json.asString
        // Firestore { "_seconds": N } object — convert to ISO string
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            val seconds = obj.get("_seconds")?.takeIf { !it.isJsonNull }?.asLong ?: return null
            return iso.format(Date(seconds * 1000L))
        }
        return null
    }
}

/** Custom Gson that handles Firestore timestamp objects in String? fields. */
private val restaurantGson = GsonBuilder()
    .registerTypeHierarchyAdapter(String::class.java, firestoreTimestampDeserializer)
    .serializeNulls()
    .create()

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
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .addInterceptor(noCacheInterceptor)
                .addInterceptor(resilientRetryInterceptor)
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
        } else {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .addInterceptor(noCacheInterceptor)
                .addInterceptor(resilientRetryInterceptor)
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
            .addConverterFactory(GsonConverterFactory.create(restaurantGson))
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
