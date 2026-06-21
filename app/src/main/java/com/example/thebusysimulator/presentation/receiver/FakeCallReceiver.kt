package com.example.thebusysimulator.presentation.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.thebusysimulator.presentation.FakeCallActivity
import com.example.thebusysimulator.presentation.di.AppContainer
import com.example.thebusysimulator.presentation.service.FakeCallNotificationService
import com.example.thebusysimulator.presentation.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver để nhận alarm và hiển thị fake call
 * - Nếu app đang chạy: Hiển thị Activity trực tiếp (không có notification)
 * - Nếu app đã đóng: Hiển thị notification với Full Screen Intent
 */
class FakeCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callerName = intent.getStringExtra(FakeCallActivity.EXTRA_CALLER_NAME) ?: "Unknown"
        val callerNumber = intent.getStringExtra(FakeCallActivity.EXTRA_CALLER_NUMBER) ?: "Unknown"
        val callId = intent.getStringExtra(FakeCallActivity.EXTRA_CALL_ID)

        android.util.Log.d("FakeCallReceiver", "🔔 Alarm triggered: $callerName ($callerNumber)")

        // Mark call as completed when alarm fires
        if (callId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AppContainer.fakeCallRepository.markCallAsCompleted(callId)
                } catch (e: Exception) {
                    android.util.Log.e("FakeCallReceiver", "Failed to mark call as completed", e)
                }
            }
        }

        // Kiểm tra xem app có đang chạy ở foreground không
        val isAppRunning = isAppInForeground(context)

        val activityIntent = Intent(context, FakeCallActivity::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, callerNumber)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        if (isAppRunning) {
            // App đang chạy → Hiển thị Activity trực tiếp (không cần notification)
            android.util.Log.d("FakeCallReceiver", "✅ App is running, showing Activity directly")
            try {
                context.startActivity(activityIntent)
                android.util.Log.d("FakeCallReceiver", "✅ Activity started directly")
            } catch (e: Exception) {
                android.util.Log.e("FakeCallReceiver", "❌ Failed to start Activity, falling back to notification", e)
                showNotification(context, callerName, callerNumber)
            }
        } else {
            // App đã đóng → LUÔN dùng Notification + Full Screen Intent (giải pháp an toàn với Google Play)
            // Đây là cách DUY NHẤT được Google Play chấp nhận, không cần overlay permission
            android.util.Log.d("FakeCallReceiver", "📱 App is closed, using Notification + Full Screen Intent (safe for Google Play)")
            showNotification(context, callerName, callerNumber)
            
            // LƯU Ý: Không dùng overlay permission khi app đã đóng vì:
            // 1. Google Play có thể từ chối app nếu dùng SYSTEM_ALERT_WINDOW
            // 2. Notification + Full Screen Intent là cách chuẩn và an toàn hơn
            // 3. Full Screen Intent tự động mở Activity khi màn hình tắt (giống cuộc gọi thật)
        }
    }

    /**
     * Kiểm tra xem app có đang chạy ở foreground không
     */
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = context.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: Kiểm tra running app processes
            val runningProcesses = activityManager.runningAppProcesses
            runningProcesses?.forEach { processInfo ->
                if (processInfo.processName == packageName) {
                    // Kiểm tra importance - FOREGROUND hoặc VISIBLE nghĩa là app đang chạy
                    val importance = processInfo.importance
                    val isForeground = importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                            importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                    android.util.Log.d("FakeCallReceiver", "Process importance: $importance, isForeground: $isForeground")
                    return isForeground
                }
            }
        } else {
            // Android < 10: Dùng getRunningTasks
            @Suppress("DEPRECATION")
            val runningTasks = activityManager.getRunningTasks(1)
            if (runningTasks.isNotEmpty()) {
                val topActivity = runningTasks[0].topActivity
                val isForeground = topActivity?.packageName == packageName
                android.util.Log.d("FakeCallReceiver", "Top activity package: ${topActivity?.packageName}, isForeground: $isForeground")
                return isForeground
            }
        }

        // Mặc định: giả sử app không chạy (an toàn hơn - dùng notification)
        android.util.Log.d("FakeCallReceiver", "App process not found, assuming app is closed")
        return false
    }

    /**
     * Hiển thị notification với Full Screen Intent
     */
    private fun showNotification(
        context: Context,
        callerName: String,
        callerNumber: String
    ) {
        try {
            FakeCallNotificationService.createNotificationChannel(context)
            FakeCallNotificationService.showIncomingCallNotification(
                context = context,
                callerName = callerName,
                callerNumber = callerNumber
            )
            android.util.Log.d("FakeCallReceiver", "✅ Notification shown with Full Screen Intent")
        } catch (e: Exception) {
            android.util.Log.e("FakeCallReceiver", "❌ Failed to show notification", e)
        }
    }
}

