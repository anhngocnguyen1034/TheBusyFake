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
import com.example.thebusysimulator.presentation.MainActivity

/**
 * Service để tạo Notification cho fake message
 * Hiển thị notification giống như tin nhắn thật từ ứng dụng nhắn tin
 */
object FakeMessageNotificationService {
    private const val CHANNEL_ID = "fake_message_channel"
    private const val CHANNEL_NAME = "Messages"
    const val NOTIFICATION_ID_BASE = 2000

    /**
     * Tạo notification channel (cần thiết cho Android O+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // HIGH để có thể hiển thị trên lock screen
            ).apply {
                description = "Notifications for fake messages"
                setShowBadge(true)
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
     * Hiển thị notification tin nhắn giả
     */
    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        notificationId: Int = NOTIFICATION_ID_BASE
    ) {
        // Intent để mở MainActivity khi tap vào notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo notification với style tin nhắn
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon) // Icon mặc định
            .setContentTitle(senderName) // Tên người gửi
            .setContentText(messageText) // Nội dung tin nhắn
            .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH priority
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Category MESSAGE
            .setAutoCancel(true) // Tự động đóng khi user tap
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiển thị trên lock screen
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
            .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)) // Notification sound
            .setContentIntent(pendingIntent) // Intent khi tap vào notification
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageText) // Hiển thị toàn bộ nội dung tin nhắn
                    .setBigContentTitle(senderName)
            )
            .build()

        // Hiển thị notification
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, notification)
                android.util.Log.d("FakeMessageNotification", "✅ Message notification displayed successfully")
                android.util.Log.d("FakeMessageNotification", "   Sender: $senderName, Message: $messageText")
            } else {
                android.util.Log.e("FakeMessageNotification", "❌ CRITICAL: Notifications are DISABLED!")
                android.util.Log.e("FakeMessageNotification", "   User must enable notifications for fake messages to work")
            }
        } catch (e: SecurityException) {
            android.util.Log.e("FakeMessageNotification", "❌ SecurityException: Cannot show notification", e)
        } catch (e: Exception) {
            android.util.Log.e("FakeMessageNotification", "❌ Error showing notification", e)
        }
    }

    /**
     * Ẩn notification
     */
    fun cancelNotification(context: Context, notificationId: Int = NOTIFICATION_ID_BASE) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}


