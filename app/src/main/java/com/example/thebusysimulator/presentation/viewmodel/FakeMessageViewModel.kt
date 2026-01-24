package com.example.thebusysimulator.presentation.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thebusysimulator.presentation.receiver.FakeMessageReceiver
import com.example.thebusysimulator.presentation.util.PermissionHelper
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
    val errorMessage: String? = null,
    val needsScheduleExactAlarmPermission: Boolean = false,
    val needsNotificationPermission: Boolean = false,
    val shouldShowPermissionDialog: Boolean = false // true nếu đã từ chối trước đó
)

class FakeMessageViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(FakeMessageUiState())
    val uiState: StateFlow<FakeMessageUiState> = _uiState.asStateFlow()

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scheduledMessages = mutableListOf<ScheduledMessage>()
    private val prefs: SharedPreferences = context.getSharedPreferences("fake_message_prefs", Context.MODE_PRIVATE)

    init {
        // Load scheduled messages from memory (có thể lưu vào SharedPreferences hoặc Room sau)
        updateUiState()
    }

    fun scheduleMessage(senderName: String, messageText: String, scheduledTime: Date) {
        viewModelScope.launch {
            try {
                // 1. Kiểm tra quyền Notification (BẮT BUỘC)
                val hasNotificationPermission = PermissionHelper.hasNotificationPermission(context)
                if (!hasNotificationPermission) {
                    android.util.Log.w("FakeMessageViewModel", "⚠️ No notification permission")
                    _uiState.value = _uiState.value.copy(needsNotificationPermission = true)
                    return@launch
                }

                // 2. Kiểm tra quyền SCHEDULE_EXACT_ALARM (từ Android 12+)
                val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true // Android < 12 không cần kiểm tra
                }

                // Nếu đã có quyền, reset flag "denied"
                if (canScheduleExact) {
                    prefs.edit().putBoolean("schedule_exact_alarm_denied", false).apply()
                }

                if (!canScheduleExact) {
                    // Kiểm tra xem đã từ chối quyền trước đó chưa
                    val hasBeenDenied = prefs.getBoolean("schedule_exact_alarm_denied", false)
                    
                    if (hasBeenDenied) {
                        // Đã từ chối trước đó: Hiện dialog giải thích
                        _uiState.value = _uiState.value.copy(
                            needsScheduleExactAlarmPermission = true,
                            shouldShowPermissionDialog = true
                        )
                    } else {
                        // Lần đầu: Mở Settings trực tiếp
                        _uiState.value = _uiState.value.copy(
                            needsScheduleExactAlarmPermission = true,
                            shouldShowPermissionDialog = false
                        )
                        // Mở Settings ngay lập tức
                        openScheduleExactAlarmSettings()
                    }
                    android.util.Log.w("FakeMessageViewModel", "⚠️ Cannot schedule exact alarms - permission not granted")
                    return@launch
                }

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
            } catch (e: SecurityException) {
                // Xử lý SecurityException khi không có quyền
                android.util.Log.e("FakeMessageViewModel", "❌ SecurityException: No permission to schedule exact alarm", e)
                _uiState.value = _uiState.value.copy(needsScheduleExactAlarmPermission = true)
            } catch (e: Exception) {
                android.util.Log.e("FakeMessageViewModel", "❌ Error scheduling message", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi lên lịch tin nhắn: ${e.message}")
            }
        }
    }
    
    fun clearPermissionRequest() {
        _uiState.value = _uiState.value.copy(
            needsScheduleExactAlarmPermission = false,
            needsNotificationPermission = false,
            shouldShowPermissionDialog = false
        )
    }
    
    fun markPermissionDenied() {
        // Đánh dấu đã từ chối quyền
        prefs.edit().putBoolean("schedule_exact_alarm_denied", true).apply()
    }
    
    fun openScheduleExactAlarmSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ có ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        } else {
            // Android < 12: Mở app settings
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    fun checkAndRequestNotificationPermission(activity: android.app.Activity, requestCode: Int) {
        if (!PermissionHelper.hasNotificationPermission(context)) {
            PermissionHelper.requestNotificationPermission(activity, requestCode)
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
                // Kiểm tra quyền Notification (BẮT BUỘC)
                val hasNotificationPermission = PermissionHelper.hasNotificationPermission(context)
                if (!hasNotificationPermission) {
                    android.util.Log.w("FakeMessageViewModel", "⚠️ No notification permission")
                    _uiState.value = _uiState.value.copy(needsNotificationPermission = true)
                    return@launch
                }
                
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




