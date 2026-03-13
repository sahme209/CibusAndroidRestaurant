package com.cibus.restaurant.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Injects "Authorization: Bearer {token}" on all API calls except auth endpoints.
 */
class RestaurantAuthInterceptor(private val tokenStore: RestaurantTokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.contains("/restaurant/auth/") || path.contains("/auth/")) {
            return chain.proceed(request)
        }
        val token = tokenStore.getToken()
        val newRequest = if (!token.isNullOrBlank()) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
