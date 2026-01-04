package com.example.thebusysimulator.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.thebusysimulator.presentation.FakeCallActivity

/**
 * Service để tạo Notification với Full Screen Intent cho fake call
 * Cách này an toàn hơn với Google Play so với SYSTEM_ALERT_WINDOW permission
 */
object FakeCallNotificationService {
    private const val CHANNEL_ID = "fake_call_channel"
    private const val CHANNEL_NAME = "Incoming Calls"
    private const val NOTIFICATION_ID = 1001

    /**
     * Tạo notification channel (cần thiết cho Android O+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // HIGH để có thể hiển thị full screen
            ).apply {
                description = "Notifications for incoming fake calls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // Cho phép sound và vibration
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Hiển thị notification với Full Screen Intent
     * Full Screen Intent sẽ tự động mở Activity khi màn hình tắt (giống cuộc gọi thật)
     */
    fun showIncomingCallNotification(
        context: Context,
        callerName: String,
        callerNumber: String
    ) {
        // Intent để mở FakeCallActivity
        val fullScreenIntent = Intent(context, FakeCallActivity::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, callerNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // PendingIntent cho Full Screen Intent (sẽ tự động mở khi màn hình tắt)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent để accept call
        val acceptIntent = Intent(context, FakeCallActivity::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, callerNumber)
            putExtra(FakeCallActivity.EXTRA_ACTION, "accept")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + 1,
            acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent để decline call
        val declineIntent = Intent(context, FakeCallActivity::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, callerNumber)
            putExtra(FakeCallActivity.EXTRA_ACTION, "decline")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + 2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming) // Icon cuộc gọi đến
            .setContentTitle(callerName)
            .setContentText(callerNumber)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH priority
            .setCategory(NotificationCompat.CATEGORY_CALL) // Category CALL
            .setFullScreenIntent(fullScreenPendingIntent, true) // Full Screen Intent - QUAN TRỌNG
            .setAutoCancel(true) // Tự động đóng khi user tap
            .setOngoing(true) // Không thể swipe away
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiển thị trên lock screen
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
            .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)) // Ringtone
            .addAction(
                android.R.drawable.ic_menu_call,
                "Accept",
                acceptPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Incoming call from $callerName")
            )
            .build()

        // Hiển thị notification
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ID, notification)
                android.util.Log.d("FakeCallNotification", "✅ Notification displayed successfully")
                android.util.Log.d("FakeCallNotification", "   Full Screen Intent will open Activity automatically")
            } else {
                // Notification bị disabled - KHÔNG THỂ start Activity trực tiếp (Android sẽ chặn)
                android.util.Log.e("FakeCallNotification", "❌ CRITICAL: Notifications are DISABLED!")
                android.util.Log.e("FakeCallNotification", "   User must enable notifications for fake calls to work")
                android.util.Log.e("FakeCallNotification", "   Cannot start Activity from background (Android restriction)")
                // Không có cách nào khác - user phải enable notifications
            }
        } catch (e: SecurityException) {
            android.util.Log.e("FakeCallNotification", "❌ SecurityException: Cannot show notification", e)
            android.util.Log.e("FakeCallNotification", "   May need USE_FULL_SCREEN_INTENT permission")
            // Không thể start Activity từ background
        } catch (e: Exception) {
            android.util.Log.e("FakeCallNotification", "❌ Error showing notification", e)
            // Không thể start Activity từ background
        }
    }

    /**
     * Ẩn notification
     */
    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}

