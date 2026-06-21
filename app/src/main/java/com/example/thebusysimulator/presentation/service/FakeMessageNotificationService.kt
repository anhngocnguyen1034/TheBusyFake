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
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.MainActivity
import com.example.thebusysimulator.presentation.util.FlashHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Service để tạo Notification cho fake message
 * Hiển thị notification giống như tin nhắn thật từ ứng dụng nhắn tin
 */
object FakeMessageNotificationService {
    private const val CHANNEL_ID = "fake_message_channel"
    private const val CHANNEL_NAME = "Messages"
    const val NOTIFICATION_ID_BASE = 2000

    private val flashScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var flashJob: Job? = null
    private var flashHelper: FlashHelper? = null

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
    fun iconResForType(appIconType: String): Int = when (appIconType) {
        "messenger" -> R.drawable.ic_notif_messenger
        "whatsapp" -> R.drawable.ic_notif_whatsapp
        "telegram" -> R.drawable.ic_notif_telegram
        "sms" -> R.drawable.ic_notif_sms
        "instagram" -> R.drawable.ic_notif_instagram
        else -> R.drawable.ic_notification
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        notificationId: Int = NOTIFICATION_ID_BASE,
        flashEnabled: Boolean = false,
        appIconType: String = "default"
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
            .setSmallIcon(iconResForType(appIconType))
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

        if (flashEnabled) {
            flashJob?.cancel()
            flashHelper?.release()
            flashHelper = FlashHelper(context)
            val helper = flashHelper!!
            flashJob = flashScope.launch {
                helper.startFlashing()
                delay(5000)
                helper.stopFlashing()
                helper.release()
            }
        }
    }

    /**
     * Ẩn notification
     */
    fun cancelNotification(context: Context, notificationId: Int = NOTIFICATION_ID_BASE) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}




