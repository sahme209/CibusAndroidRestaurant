package com.cibus.restaurant.api

/**
 * Shared callback for 401/session invalidation.
 * RestaurantApp sets on401 when mounted; RestaurantAuthInterceptor invokes it on 401.
 */
object RestaurantSessionCallbacks {
    @Volatile
    var on401: (() -> Unit)? = null
}
