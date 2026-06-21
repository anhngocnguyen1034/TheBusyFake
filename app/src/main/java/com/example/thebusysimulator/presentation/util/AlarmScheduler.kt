package com.example.thebusysimulator.presentation.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.domain.util.CallScheduler
import com.example.thebusysimulator.presentation.FakeCallActivity
import com.example.thebusysimulator.presentation.receiver.FakeCallReceiver

class AlarmSchedulerImpl(private val context: Context) : CallScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(fakeCall: FakeCall) {
        android.util.Log.d("AlarmScheduler", "📅 Scheduling alarm for: ${fakeCall.callerName}")
        android.util.Log.d("AlarmScheduler", "   Time: ${fakeCall.scheduledTime}")
        android.util.Log.d("AlarmScheduler", "   Call ID: ${fakeCall.id}")
        
        val intent = Intent(context, FakeCallReceiver::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, fakeCall.callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, fakeCall.callerNumber)
            putExtra(FakeCallActivity.EXTRA_CALL_ID, fakeCall.id)
            action = "com.example.thebusysimulator.FAKE_CALL_ALARM"
        }

        // Tạo PendingIntent với requestCode duy nhất để tránh conflict
        val requestCode = fakeCall.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        android.util.Log.d("AlarmScheduler", "   PendingIntent created with requestCode: $requestCode")

        // Kiểm tra quyền SCHEDULE_EXACT_ALARM (từ Android 12+)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Android < 12 không cần kiểm tra
        }

        if (!canScheduleExact) {
            android.util.Log.w("AlarmScheduler", "⚠️ Cannot schedule exact alarms - permission not granted")
            android.util.Log.w("AlarmScheduler", "   User needs to grant SCHEDULE_EXACT_ALARM in Settings")
            // Fallback sang inexact alarm
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        fakeCall.scheduledTime.time,
                        pendingIntent
                    )
                    android.util.Log.w("AlarmScheduler", "⚠️ Using inexact alarm (may be delayed)")
                } else {
                    @Suppress("DEPRECATION")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        fakeCall.scheduledTime.time,
                        pendingIntent
                    )
                    android.util.Log.w("AlarmScheduler", "⚠️ Using deprecated set() method")
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmScheduler", "❌ CRITICAL: Cannot schedule alarm at all!", e)
            }
            return
        }

        // Dùng setExactAndAllowWhileIdle để đảm bảo alarm vẫn hoạt động khi app đã đóng
        // và thiết bị ở chế độ Doze
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+: Dùng setExactAndAllowWhileIdle để hoạt động trong Doze mode
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    fakeCall.scheduledTime.time,
                    pendingIntent
                )
            } else {
                // Android < 6.0: Dùng setExact
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    fakeCall.scheduledTime.time,
                    pendingIntent
                )
            }
            android.util.Log.d("AlarmScheduler", "✅ Alarm scheduled successfully for ${fakeCall.callerName} at ${fakeCall.scheduledTime}")
            android.util.Log.d("AlarmScheduler", "   Alarm will trigger even when app is closed")
        } catch (e: SecurityException) {
            android.util.Log.e("AlarmScheduler", "❌ SecurityException: Cannot schedule exact alarm", e)
            // Fallback nếu không có permission SCHEDULE_EXACT_ALARM (hoặc user chưa cấp quyền)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        fakeCall.scheduledTime.time,
                        pendingIntent
                    )
                    android.util.Log.w("AlarmScheduler", "⚠️ Using inexact alarm as fallback (may be delayed)")
                } else {
                    @Suppress("DEPRECATION")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        fakeCall.scheduledTime.time,
                        pendingIntent
                    )
                    android.util.Log.w("AlarmScheduler", "⚠️ Using deprecated set() method")
                }
            } catch (e2: Exception) {
                android.util.Log.e("AlarmScheduler", "❌ CRITICAL: Cannot schedule alarm at all!", e2)
            }
        } catch (e: Exception) {
            android.util.Log.e("AlarmScheduler", "❌ Unexpected error scheduling alarm", e)
        }
    }

    override fun cancel(callId: String) {
        val intent = Intent(context, FakeCallReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun testCallNow(callerName: String, callerNumber: String, isVideoCall: Boolean) {
        // Start Activity directly
        val activityIntent = Intent(context, FakeCallActivity::class.java).apply {
            putExtra(FakeCallActivity.EXTRA_CALLER_NAME, callerName)
            putExtra(FakeCallActivity.EXTRA_CALLER_NUMBER, callerNumber)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(activityIntent)
    }
}
