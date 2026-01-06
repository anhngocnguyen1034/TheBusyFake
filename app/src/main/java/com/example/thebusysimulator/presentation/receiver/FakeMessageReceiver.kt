package com.example.thebusysimulator.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.thebusysimulator.presentation.service.FakeMessageNotificationService

/**
 * Receiver để nhận alarm và hiển thị fake message notification
 */
class FakeMessageReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_SENDER_NAME = "sender_name"
        const val EXTRA_MESSAGE_TEXT = "message_text"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val senderName = intent.getStringExtra(EXTRA_SENDER_NAME) ?: "Unknown"
        val messageText = intent.getStringExtra(EXTRA_MESSAGE_TEXT) ?: "You have a new message"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, FakeMessageNotificationService.NOTIFICATION_ID_BASE)

        android.util.Log.d("FakeMessageReceiver", "🔔 Message alarm triggered: $senderName - $messageText")

        try {
            FakeMessageNotificationService.createNotificationChannel(context)
            FakeMessageNotificationService.showMessageNotification(
                context = context,
                senderName = senderName,
                messageText = messageText,
                notificationId = notificationId
            )
            android.util.Log.d("FakeMessageReceiver", "✅ Message notification shown successfully")
        } catch (e: Exception) {
            android.util.Log.e("FakeMessageReceiver", "❌ Failed to show message notification", e)
        }
    }
}



