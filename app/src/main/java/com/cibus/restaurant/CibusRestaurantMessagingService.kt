package com.cibus.restaurant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cibus.restaurant.api.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles incoming FCM messages for the Cibus Restaurant app.
 *
 * Supported event_type values from the backend:
 *   - "new_order" / "new_order_incoming"  → new order received
 *   - "order_cancelled"                   → customer cancelled
 *   - generic                             → show notification with body text
 *
 * Token registration happens here (onNewToken) and also at login time
 * via [registerRestaurantFcmToken].
 */
class CibusRestaurantMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data      = message.data
        val eventType = data["event_type"] ?: data["type"] ?: ""
        val orderId   = data["order_id"] ?: data["orderId"] ?: ""

        val (title, body) = when {
            eventType.contains("new_order", ignoreCase = true) ->
                "New Order!" to "A new order has arrived. Open the app to accept."
            eventType.contains("cancel", ignoreCase = true) ->
                "Order Cancelled" to "An order has been cancelled by the customer."
            message.notification != null ->
                (message.notification?.title ?: "Cibus Restaurant") to
                (message.notification?.body  ?: "You have a new update.")
            else ->
                "Cibus Restaurant" to (data["body"] ?: "You have a new update.")
        }

        showNotification(title, body, orderId)
    }

    override fun onNewToken(token: String) {
        registerRestaurantFcmToken(applicationContext, token)
    }

    private fun showNotification(title: String, body: String, orderId: String) {
        val manager   = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "cibus_restaurant_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Orders", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Cibus Restaurant order notifications" }
            )
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (orderId.isNotEmpty()) putExtra("orderId", orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(orderId.hashCode().takeIf { it != 0 } ?: 2, notification)
    }
}

// ── Token registration helper ─────────────────────────────────────────────────

/**
 * Fetches the current FCM token and registers it with the Cibus backend.
 * Call this after login and from [CibusRestaurantMessagingService.onNewToken].
 */
fun registerRestaurantFcmToken(context: Context, token: String? = null) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val tokenStore = RetrofitClient.getTokenStore()
            if (!tokenStore.hasValidToken()) return@launch

            val fcmToken = token ?: run {
                var result: String? = null
                FirebaseMessaging.getInstance().token.addOnSuccessListener { result = it }
                result
            } ?: return@launch

            RetrofitClient.restaurantApi.registerDeviceToken(
                mapOf("token" to fcmToken, "platform" to "fcm")
            )
        } catch (_: Exception) { /* non-fatal — push degrades to polling */ }
    }
}
