package com.example.thebusysimulator.presentation.util

object AutoReplyHelper {
    // Danh sách câu trả lời tự động cho "Mẹ"
    private val momReplies = listOf(
        "Con ơi, mẹ đang bận một chút. Con có cần gì không?",
        "Mẹ nhớ con lắm. Con ăn cơm chưa?",
        "Con về nhà sớm nhé, mẹ nấu món con thích đấy.",
        "Mẹ đang đi chợ, con cần mẹ mua gì không?",
        "Con học bài xong chưa? Đừng thức khuya quá nhé.",
        "Mẹ thấy con gần đây có vẻ mệt. Con có ổn không?",
        "Con nhớ uống nước đầy đủ nhé. Sức khỏe quan trọng lắm.",
        "Mẹ đang nấu ăn, lát nữa về nhà ăn cơm với mẹ nhé.",
        "Con có nhớ hôm nay là ngày gì không? Mẹ nhớ con lắm.",
        "Mẹ đang nghĩ về con. Con có muốn nói chuyện với mẹ không?",
        "Con đừng quên mặc ấm nhé, trời lạnh rồi đấy.",
        "Mẹ vừa làm bánh, con về nhà mẹ cho ăn nhé.",
        "Con có bạn bè đến chơi không? Mẹ sẽ chuẩn bị đồ ăn.",
        "Mẹ thấy con học hành vất vả. Cố gắng nhưng đừng quá sức nhé.",
        "Con nhớ gọi điện cho mẹ khi về nhà nhé.",
        "Mẹ đang dọn dẹp nhà cửa. Con về giúp mẹ nhé.",
        "Con có muốn mẹ nấu món gì đặc biệt không?",
        "Mẹ nhớ những lúc con còn nhỏ. Thời gian trôi nhanh quá.",
        "Con đừng quên tập thể dục nhé. Sức khỏe là vàng.",
        "Mẹ yêu con nhiều lắm. Con là niềm tự hào của mẹ."
    )
    
    // Tin nhắn mặc định khi khởi động
    val defaultMomMessages = listOf(
        "Con ơi, mẹ nhớ con lắm. Con có khỏe không?",
        "Mẹ vừa nấu món con thích đấy. Con về nhà sớm nhé.",
        "Con đừng quên ăn đủ bữa nhé. Mẹ lo lắng lắm."
    )
    
    // Danh sách câu trả lời tự động cho "Người yêu"
    private val loverReplies = listOf(
        "Mình nhớ bạn lắm. Bạn đang làm gì thế?",
        "Hôm nay bạn có rảnh không? Mình đi chơi nhé.",
        "Mình vừa nghĩ về bạn. Bạn có nhớ mình không?",
        "Mình yêu bạn nhiều lắm. Bạn biết không?",
        "Hôm nay mình thấy buồn. Bạn có thể nói chuyện với mình không?",
        "Mình muốn gặp bạn. Khi nào mình gặp nhau nhé?",
        "Bạn đang làm gì vậy? Mình nhớ bạn quá.",
        "Mình vừa xem phim hay lắm. Mình cùng xem nhé.",
        "Bạn có đói không? Mình nấu món bạn thích nhé.",
        "Mình thấy hôm nay bạn đẹp quá. Mình thích lắm.",
        "Bạn có muốn đi cafe không? Mình rất muốn gặp bạn.",
        "Mình đang nghe nhạc buồn. Bạn có thể ở bên mình không?",
        "Bạn là người quan trọng nhất với mình. Mình yêu bạn nhiều.",
        "Mình vừa mua quà cho bạn. Bạn có muốn biết không?",
        "Hôm nay mình thấy hạnh phúc vì có bạn. Cảm ơn bạn nhé.",
        "Bạn có nhớ lần đầu mình gặp nhau không? Mình nhớ lắm.",
        "Mình muốn được ở bên bạn mãi. Bạn có muốn không?",
        "Bạn đừng quên ăn đủ bữa nhé. Mình lo lắng lắm.",
        "Mình thấy hôm nay bạn có vẻ mệt. Bạn có ổn không?",
        "Bạn là lý do mình thức dậy mỗi sáng với nụ cười. Mình yêu bạn."
    )
    
    // Tin nhắn mặc định khi khởi động cho người yêu
    val defaultLoverMessages = listOf(
        "Mình nhớ bạn lắm. Bạn có khỏe không?",
        "Hôm nay mình muốn gặp bạn. Mình đi chơi nhé.",
        "Mình yêu bạn nhiều lắm. Bạn biết không?"
    )
    
    // Danh sách câu trả lời tự động cho các contact tự tạo (generic)
    private val genericReplies = listOf(
        "Ồ, mình vừa thấy tin nhắn của bạn. Bạn đang làm gì thế?",
        "Cảm ơn bạn đã nhắn tin. Mình đang bận một chút, lát nữa trả lời nhé.",
        "Mình thấy tin nhắn rồi. Bạn có cần gì không?",
        "Xin chào! Mình đang ở đây. Bạn muốn nói gì không?",
        "Mình vừa xem tin nhắn. Bạn có khỏe không?",
        "Cảm ơn bạn đã liên lạc. Mình sẽ trả lời sớm nhé.",
        "Mình thấy tin nhắn rồi. Hôm nay bạn thế nào?",
        "Xin chào! Mình đang nghe đây. Bạn nói đi.",
        "Mình vừa đọc tin nhắn. Bạn có muốn gặp không?",
        "Cảm ơn bạn. Mình đang suy nghĩ về điều bạn nói.",
        "Mình thấy tin nhắn rồi. Bạn có rảnh không?",
        "Xin chào! Mình đang ở đây. Bạn cần gì không?",
        "Mình vừa xem tin nhắn. Bạn có muốn nói chuyện không?",
        "Cảm ơn bạn đã nhắn. Mình sẽ trả lời ngay.",
        "Mình thấy tin nhắn rồi. Bạn đang ở đâu thế?",
        "Xin chào! Mình đang nghe. Bạn nói tiếp đi.",
        "Mình vừa đọc tin nhắn. Bạn có khỏe không?",
        "Cảm ơn bạn. Mình đang suy nghĩ.",
        "Mình thấy tin nhắn rồi. Bạn muốn làm gì?",
        "Xin chào! Mình đang ở đây. Bạn nói đi."
    )
    
    /**
     * Lấy câu trả lời tiếp theo dựa trên index cho Mẹ
     * Index sẽ được lưu trong database hoặc state
     */
    fun getNextReply(replyIndex: Int): String {
        return if (replyIndex < momReplies.size) {
            momReplies[replyIndex]
        } else {
            // Nếu hết câu trả lời, quay lại từ đầu
            momReplies[replyIndex % momReplies.size]
        }
    }
    
    /**
     * Lấy câu trả lời tiếp theo dựa trên index cho Người yêu
     * Index sẽ được lưu trong database hoặc state
     */
    fun getNextLoverReply(replyIndex: Int): String {
        return if (replyIndex < loverReplies.size) {
            loverReplies[replyIndex]
        } else {
            // Nếu hết câu trả lời, quay lại từ đầu
            loverReplies[replyIndex % loverReplies.size]
        }
    }
    
    /**
     * Kiểm tra xem contact có phải là "Mẹ" không
     */
    fun isMomContact(contactName: String): Boolean {
        return contactName.equals("Mẹ", ignoreCase = true) || 
               contactName.equals("Me", ignoreCase = true) ||
               contactName.equals("Mom", ignoreCase = true) ||
               contactName.equals("Mother", ignoreCase = true)
    }
    
    /**
     * Kiểm tra xem contact có phải là "Người yêu" không
     */
    fun isLoverContact(contactName: String): Boolean {
        val name = contactName.lowercase()
        return name.contains("người yêu") || name.contains("nguoi yeu") ||
               name.contains("lover") || name.contains("boyfriend") ||
               name.contains("girlfriend") || name.contains("crush") ||
               name.contains("bạn trai") || name.contains("ban trai") ||
               name.contains("bạn gái") || name.contains("ban gai") ||
               name.equals("ny", ignoreCase = true) ||
               name.equals("người yêu", ignoreCase = true)
    }
    
    /**
     * Xác định loại nhân vật từ tên liên hệ
     * @param contactName Tên liên hệ
     * @return CharacterType tương ứng
     */
    fun getCharacterType(contactName: String): CharacterType {
        val name = contactName.lowercase()
        
        // Bố, Mẹ
        if (name.contains("mẹ") || name.contains("me") || 
            name.contains("mom") || name.contains("mother") ||
            name.contains("bố") || name.contains("bo") || 
            name.contains("dad") || name.contains("father")) {
            return CharacterType.PARENT
        }
        
        // Người yêu
        if (name.contains("người yêu") || name.contains("nguoi yeu") ||
            name.contains("lover") || name.contains("boyfriend") ||
            name.contains("girlfriend") || name.contains("crush") ||
            name.contains("bạn trai") || name.contains("ban trai") ||
            name.contains("bạn gái") || name.contains("ban gai")) {
            return CharacterType.LOVER
        }
        
        // Game thủ, Cầu thủ
        if (name.contains("game thủ") || name.contains("game thu") ||
            name.contains("gamer") || name.contains("player") ||
            name.contains("cầu thủ") || name.contains("cau thu") ||
            name.contains("footballer") || name.contains("athlete")) {
            return CharacterType.YOUTH
        }
        
        // Bác sĩ, Nha sĩ, Nhà khoa học
        if (name.contains("bác sĩ") || name.contains("bac si") ||
            name.contains("doctor") || name.contains("nha sĩ") ||
            name.contains("nha si") || name.contains("dentist") ||
            name.contains("nhà khoa học") || name.contains("nha khoa hoc") ||
            name.contains("scientist") || name.contains("professor")) {
            return CharacterType.EXPERT
        }
        
        // Mặc định: Nếu là "Mẹ" thì PARENT, còn lại là LOVER
        return if (isMomContact(contactName)) {
            CharacterType.PARENT
        } else {
            CharacterType.LOVER
        }
    }
    
    /**
     * Lấy tổng số câu trả lời cho Mẹ
     */
    fun getTotalReplies(): Int = momReplies.size
    
    /**
     * Lấy tổng số câu trả lời cho Người yêu
     */
    fun getTotalLoverReplies(): Int = loverReplies.size
    
    /**
     * Lấy câu trả lời tiếp theo dựa trên index cho các contact tự tạo (generic)
     * Index sẽ được lưu trong database hoặc state
     */
    fun getNextGenericReply(replyIndex: Int): String {
        return if (replyIndex < genericReplies.size) {
            genericReplies[replyIndex]
        } else {
            // Nếu hết câu trả lời, quay lại từ đầu
            genericReplies[replyIndex % genericReplies.size]
        }
    }
    
    /**
     * Lấy tổng số câu trả lời cho các contact tự tạo
     */
    fun getTotalGenericReplies(): Int = genericReplies.size
}

