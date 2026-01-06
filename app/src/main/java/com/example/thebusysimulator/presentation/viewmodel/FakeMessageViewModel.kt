package com.example.thebusysimulator.presentation.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thebusysimulator.presentation.receiver.FakeMessageReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class ScheduledMessage(
    val id: String,
    val senderName: String,
    val messageText: String,
    val scheduledTime: Date
)

data class FakeMessageUiState(
    val scheduledMessages: List<ScheduledMessage> = emptyList(),
    val errorMessage: String? = null
)

class FakeMessageViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(FakeMessageUiState())
    val uiState: StateFlow<FakeMessageUiState> = _uiState.asStateFlow()

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scheduledMessages = mutableListOf<ScheduledMessage>()

    init {
        // Load scheduled messages from memory (có thể lưu vào SharedPreferences hoặc Room sau)
        updateUiState()
    }

    fun scheduleMessage(senderName: String, messageText: String, scheduledTime: Date) {
        viewModelScope.launch {
            try {
                val messageId = UUID.randomUUID().toString()
                val message = ScheduledMessage(
                    id = messageId,
                    senderName = senderName,
                    messageText = messageText,
                    scheduledTime = scheduledTime
                )

                // Lên lịch alarm
                val intent = Intent(context, FakeMessageReceiver::class.java).apply {
                    putExtra(FakeMessageReceiver.EXTRA_SENDER_NAME, senderName)
                    putExtra(FakeMessageReceiver.EXTRA_MESSAGE_TEXT, messageText)
                    putExtra(FakeMessageReceiver.EXTRA_NOTIFICATION_ID, messageId.hashCode())
                    action = "com.example.thebusysimulator.FAKE_MESSAGE_ALARM"
                }

                val requestCode = messageId.hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Dùng setExactAndAllowWhileIdle để đảm bảo alarm vẫn hoạt động khi app đã đóng
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime.time,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime.time,
                        pendingIntent
                    )
                }

                scheduledMessages.add(message)
                updateUiState()
                android.util.Log.d("FakeMessageViewModel", "✅ Message scheduled: $senderName - $messageText")
            } catch (e: Exception) {
                android.util.Log.e("FakeMessageViewModel", "❌ Error scheduling message", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi lên lịch tin nhắn: ${e.message}")
            }
        }
    }

    fun cancelMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val message = scheduledMessages.find { it.id == messageId }
                if (message != null) {
                    val intent = Intent(context, FakeMessageReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        messageId.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                    scheduledMessages.remove(message)
                    updateUiState()
                    android.util.Log.d("FakeMessageViewModel", "✅ Message cancelled: $messageId")
                }
            } catch (e: Exception) {
                android.util.Log.e("FakeMessageViewModel", "❌ Error cancelling message", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi hủy tin nhắn: ${e.message}")
            }
        }
    }

    fun showMessageNow(senderName: String, messageText: String) {
        viewModelScope.launch {
            try {
                com.example.thebusysimulator.presentation.service.FakeMessageNotificationService.createNotificationChannel(context)
                com.example.thebusysimulator.presentation.service.FakeMessageNotificationService.showMessageNotification(
                    context = context,
                    senderName = senderName,
                    messageText = messageText
                )
                android.util.Log.d("FakeMessageViewModel", "✅ Message shown immediately: $senderName - $messageText")
            } catch (e: Exception) {
                android.util.Log.e("FakeMessageViewModel", "❌ Error showing message", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi hiển thị tin nhắn: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun updateUiState() {
        _uiState.value = _uiState.value.copy(
            scheduledMessages = scheduledMessages.sortedBy { it.scheduledTime }
        )
    }
}



