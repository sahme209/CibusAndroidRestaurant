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
