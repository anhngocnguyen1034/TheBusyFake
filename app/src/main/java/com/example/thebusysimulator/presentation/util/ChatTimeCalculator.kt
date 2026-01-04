package com.example.thebusysimulator.presentation.util

import kotlin.random.Random

/**
 * Enum để phân loại nhân vật
 */
enum class CharacterType {
    PARENT,     // Bố, Mẹ
    LOVER,      // Người yêu
    EXPERT,     // Bác sĩ, Nha sĩ, Nhà khoa học
    YOUTH       // Game thủ, Cầu thủ
}

/**
 * Object tính toán thời gian delay cho tin nhắn dựa trên:
 * - Độ dài tin nhắn
 * - Loại nhân vật (tốc độ gõ khác nhau)
 * - Thời gian suy nghĩ/đọc
 * - Yếu tố ngẫu nhiên
 */
object ChatTimeCalculator {

    /**
     * Tính toán thời gian delay để hiển thị tin nhắn
     * 
     * @param message Nội dung tin nhắn cần gửi
     * @param type Loại nhân vật (PARENT, LOVER, EXPERT, YOUTH)
     * @return Thời gian delay tính bằng milliseconds (giới hạn từ 1500ms đến 6000ms)
     */
    fun calculateDelay(message: String, type: CharacterType): Long {
        // 1. Xác định tốc độ gõ (ms trên mỗi ký tự)
        val msPerChar = when (type) {
            CharacterType.YOUTH -> Random.nextLong(50, 81)   // Nhanh: 50-80ms/ký tự
            CharacterType.LOVER -> Random.nextLong(100, 121)  // Trung bình: 100-120ms/ký tự
            CharacterType.EXPERT -> 150L                      // Hơi chậm: 150ms/ký tự
            CharacterType.PARENT -> Random.nextLong(200, 301) // Rất chậm: 200-300ms/ký tự (mổ cò)
        }

        // 2. Thời gian suy nghĩ/đọc (cố định khoảng 500ms - 1000ms)
        val thinkingTime = Random.nextLong(500, 1001)

        // 3. Tính toán thời gian gõ cơ bản
        val typingTime = message.length * msPerChar

        // 4. Tổng thời gian
        var totalDelay = thinkingTime + typingTime

        // 5. Thêm yếu tố ngẫu nhiên (Random) để tự nhiên hơn
        // Cộng/Trừ thêm khoảng -200ms đến 500ms
        val randomJitter = Random.nextLong(-200, 501)
        totalDelay += randomJitter

        // 6. QUAN TRỌNG: Thiết lập giới hạn (Min/Max)
        // Nhanh nhất cũng phải 1.5s, Chậm nhất không quá 6s (để user đỡ chờ lâu)
        return totalDelay.coerceIn(1500L, 6000L)
    }
}

