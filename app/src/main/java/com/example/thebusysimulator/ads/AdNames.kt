package com.example.thebusysimulator.ads

import com.anhnn.ads.AdFormat

/**
 * Tên (khoá) cho từng vị trí quảng cáo trong app.
 *
 * Mỗi tên là 1 key trong JSON [RemoteConfigManager.KEY_AD_UNITS] map sang 1 ad_unit_id
 * riêng trên Firebase Remote Console. Khi key chưa có trong config → module tự dùng
 * test unit của Google theo [formatOf]. Không hardcode ad_unit_id ở đây.
 */
object AdNames {

    // --- App Open ---
    /** Hiện khi user quay lại app từ background. */
    const val APP_OPEN_RESUME = "app_open_resume"

    // --- Interstitial (toàn màn) ---
    /** Sau màn Splash, trước khi vào Home. */
    const val SPLASH_OPEN = "splash_open"

    /** Khi mở tính năng từ Home. */
    const val HOME_FAKE_CALL = "home_fake_call"
    const val HOME_FAKE_CHAT = "home_fake_chat"
    const val HOME_NOTIFICATION = "home_notification"

    // --- Banner ---
    /** Cuối màn danh sách hội thoại (Fake Chat). */
    const val MESSAGE_BANNER = "message_banner"

    /** Cuối màn Lịch sử cuộc gọi. */
    const val CALL_HISTORY_BANNER = "call_history_banner"

    /** Cuối màn Lịch sử thông báo. */
    const val NOTIFICATION_HISTORY_BANNER = "notification_history_banner"

    /** Định dạng của [name]; null nếu tên không hợp lệ. */
    fun formatOf(name: String): AdFormat? = when (name) {
        APP_OPEN_RESUME -> AdFormat.APP_OPEN
        SPLASH_OPEN,
        HOME_FAKE_CALL,
        HOME_FAKE_CHAT,
        HOME_NOTIFICATION -> AdFormat.INTERSTITIAL
        MESSAGE_BANNER,
        CALL_HISTORY_BANNER,
        NOTIFICATION_HISTORY_BANNER -> AdFormat.BANNER
        else -> null
    }
}
