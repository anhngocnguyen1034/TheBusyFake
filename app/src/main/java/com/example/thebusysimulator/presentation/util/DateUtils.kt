package com.example.thebusysimulator.presentation.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    fun formatMessageTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            // 1. Cùng ngày (Hôm nay) -> 14:30
            isSameDay(now, msgTime) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgTime.time)
            }
            
            isYesterday(now, msgTime) -> {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgTime.time)
                "$time"
            }

            // 3. Trong tuần (cách đây dưới 7 ngày) -> T2 14:30
            isSameWeek(now, msgTime) -> {
                // EEE sẽ trả về "Th 2", "Mon"... tùy Locale
                SimpleDateFormat("EEE HH:mm", Locale("vi", "VN")).format(msgTime.time)
            }

            // 4. Cùng năm -> 05/01 14:30 (hoặc chỉ 05/01 tùy bạn)
            isSameYear(now, msgTime) -> {
                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(msgTime.time)
            }

            // 5. Khác năm -> 05/01/2024
            else -> {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(msgTime.time)
            }
        }
    }

    // Overload function để nhận Date
    fun formatMessageTime(date: Date): String {
        return formatMessageTime(date.time)
    }

    // Hàm để lấy label header theo ngày (Hôm nay, Hôm qua, hoặc ngày tháng)
    fun getDateHeaderLabel(date: Date): String {
        val now = Calendar.getInstance()
        val msgTime = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(now, msgTime) -> "Hôm nay"
            isYesterday(now, msgTime) -> "Hôm qua"
            isSameWeek(now, msgTime) -> {
                // Thứ trong tuần
                SimpleDateFormat("EEEE", Locale("vi", "VN")).format(date)
            }
            isSameYear(now, msgTime) -> {
                // Ngày/tháng
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
            else -> {
                // Ngày/tháng/năm
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
        }
    }

    // Hàm để kiểm tra 2 Date có cùng ngày không (dùng cho header)
    fun isSameDate(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return isSameDay(cal1, cal2)
    }

    // --- Các hàm kiểm tra logic ---

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, msgTime: Calendar): Boolean {
        // Clone để không ảnh hưởng biến gốc
        val yesterday = now.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, msgTime)
    }

    private fun isSameWeek(now: Calendar, msgTime: Calendar): Boolean {
        // Kiểm tra xem có cùng năm và tuần trong năm không, hoặc khoảng cách < 7 ngày
        val diff = now.timeInMillis - msgTime.timeInMillis
        return diff < TimeUnit.DAYS.toMillis(7) && diff > 0
    }

    private fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}

