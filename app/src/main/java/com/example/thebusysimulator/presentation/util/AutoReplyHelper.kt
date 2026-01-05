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
    
    // Danh sách câu trả lời tự động cho "Bác sĩ"
    private val doctorReplies = listOf(
        "Bạn nhớ kiểm tra xem đã uống đủ nước chưa? Não bộ chứa khoảng 73% là nước, mất nước nhẹ cũng làm giảm trí nhớ và sự tập trung đấy.",
        "Nếu bạn đang nhìn màn hình lâu, hãy áp dụng quy tắc 20-20-20: Cứ 20 phút, nhìn xa 6 mét trong 20 giây để giảm cận thị giả.",
        "Thử bài tập thở này xem: Hít vào bằng mũi 4 giây, giữ hơi 7 giây, rồi thở ra bằng miệng 8 giây. Nó giúp hệ thần kinh thư giãn ngay lập tức.",
        "Bạn có đang ngồi gù lưng không? Ngồi sai tư thế chèn ép phổi, giảm oxy lên não khiến bạn buồn ngủ. Hãy đứng dậy vươn vai đi nào!",
        "Khi stress, cơ thể tiết ra Cortisol. Nếu nồng độ này cao quá lâu sẽ gây tích mỡ bụng và suy giảm miễn dịch. Hãy thả lỏng vai xuống.",
        "Tối nay nhớ ngủ trước 11 giờ nhé. Khi ngủ, hệ thống Glymphatic của não sẽ mở ra để 'dọn rác' độc tố tích tụ trong ngày.",
        "Bữa sáng khởi động quá trình trao đổi chất. Bỏ bữa sáng giống như bắt xe chạy mà không đổ xăng vậy.",
        "Hạn chế đồ uống quá ngọt vào buổi chiều. Đường sẽ làm năng lượng tăng vọt rồi tụt dốc không phanh (Sugar crash), khiến bạn uể oải hơn.",
        "Cơ thể con người được thiết kế để vận động. Hãy đi lại nhẹ nhàng mỗi 1 tiếng để giảm nguy cơ tim mạch.",
        "Tối về nhà, hãy hạn chế nhìn điện thoại 1 tiếng trước khi ngủ. Ánh sáng xanh ức chế Melatonin, làm bạn khó đi vào giấc ngủ sâu.",
        "Nếu có thể, hãy ra ngoài đón chút nắng sớm. Ánh nắng giúp tổng hợp Vitamin D và Serotonin giúp bạn cảm thấy hạnh phúc hơn.",
        "Đường ruột là 'bộ não thứ hai' của cơ thể. Hãy ăn thêm sữa chua hoặc rau xanh. Bụng êm thì đầu óc mới sáng suốt được.",
        "Đừng uống cà phê sau 3 giờ chiều. Caffeine cần tới 6-8 tiếng để đào thải một nửa lượng đã nạp, uống muộn sẽ làm hỏng giấc ngủ tối.",
        "Đôi khi liều thuốc tốt nhất cho tâm trí là 'Off' thông báo điện thoại. Dành 15 phút không công nghệ để não bộ được nghỉ ngơi thực sự.",
        "Đừng lờ đi những cơn đau nhỏ như mỏi cổ, tê tay. Đó là tín hiệu cầu cứu của cơ thể báo hiệu bạn cần nghỉ ngơi ngay.",
        "Mỗi ngày hãy thử tìm một điều nhỏ bé làm bạn vui. Tư duy tích cực thực sự giúp tăng cường hệ miễn dịch đấy.",
        "Mẹo ngủ ngon: Phòng ngủ nên hơi lạnh một chút (khoảng 20-22 độ C). Nhiệt độ cơ thể cần giảm xuống để bắt đầu giấc ngủ sâu.",
        "Công việc thì làm cả đời, nhưng sức khỏe có hạn sử dụng. Đừng bán mạng kiếm tiền rồi lại dùng tiền mua sức khỏe.",
        "Hãy chớp mắt thường xuyên hơn. Khi tập trung vào màn hình, tần số chớp mắt giảm 66% gây khô và mỏi mắt.",
        "Một nụ cười giúp giãn cơ mặt và giảm hormone căng thẳng. Dù có đang bận, hãy thử cười một cái xem sao!"
    )
    
    // Tin nhắn mặc định khi khởi động cho bác sĩ
    val defaultDoctorMessages = listOf(
        "Bạn nhớ kiểm tra xem đã uống đủ nước chưa? Não bộ chứa khoảng 73% là nước, mất nước nhẹ cũng làm giảm trí nhớ và sự tập trung đấy.",
        "Nếu bạn đang nhìn màn hình lâu, hãy áp dụng quy tắc 20-20-20: Cứ 20 phút, nhìn xa 6 mét trong 20 giây để giảm cận thị giả."
    )
    
    // Danh sách câu trả lời tự động cho "Nhà khoa học"
    private val scientistReplies = listOf(
        "Bạn thực chất là 'bụi sao'. Các nguyên tử Carbon, Nitơ trong cơ thể bạn được tạo ra từ lò phản ứng hạt nhân của các ngôi sao đã chết hàng tỷ năm trước.",
        "Một ngày trên Sao Kim dài hơn cả một năm trên Sao Kim. Nó quay quanh trục mất 243 ngày Trái Đất, nhưng quay quanh Mặt Trời chỉ mất 225 ngày.",
        "Khi nhìn lên bầu trời, bạn đang nhìn về quá khứ. Ánh sáng Mặt Trời mất 8 phút 20 giây để đến đây. Nếu Mặt Trời tắt, 8 phút sau chúng ta mới biết.",
        "Nếu tháo xoắn toàn bộ DNA trong cơ thể một người và nối lại, nó sẽ dài khoảng 100 tỷ dặm. Đủ để đi từ Trái Đất đến sao Diêm Vương và quay lại hơn 10 lần.",
        "Thế giới này cực kỳ rỗng. Nguyên tử chứa 99.9999% là khoảng trống. Nếu loại bỏ hết khoảng trống đó, 8 tỷ người sẽ bị nén vừa vào một viên đường.",
        "Bạch tuộc có tới 3 quả tim, 9 bộ não và máu của chúng màu xanh lam do chứa đồng thay vì sắt như con người.",
        "Cá mập đã tồn tại trên Trái Đất lâu hơn cả cây cối. Cá mập xuất hiện khoảng 400 triệu năm trước, còn cây cối thì khoảng 350 triệu năm thôi.",
        "Nữ hoàng Cleopatra sống gần thời đại phát minh ra iPhone hơn là thời đại xây dựng Kim tự tháp Giza.",
        "Một đám mây trắng xốp bình thường nặng trung bình khoảng 500 tấn (tương đương 100 con voi). Nó bay được là nhờ không khí nóng nâng đỡ.",
        "Chuối là thực phẩm có tính phóng xạ nhẹ vì chứa Kali-40. Nhưng yên tâm, bạn phải ăn 10 triệu quả chuối cùng lúc mới bị ngộ độc.",
        "Sao Neutron đặc đến mức: Một thìa cà phê vật chất của nó nặng khoảng 6 tỷ tấn - tương đương cân nặng của tất cả con người trên Trái Đất cộng lại.",
        "Trong cơ thể bạn, số lượng vi khuẩn còn nhiều hơn cả số tế bào con người. Về mặt kỹ thuật, bạn là 'nhà trọ' khổng lồ cho vi khuẩn.",
        "Cốc nước bạn đang uống có thể chứa các phân tử nước 'già' hơn cả Mặt Trời. Nước trên Trái Đất vốn trôi dạt trong không gian trước khi Hệ Mặt Trời hình thành.",
        "Bộ não của bạn khi thức tạo ra khoảng 12-25 watt điện. Đủ để thắp sáng một bóng đèn LED nhỏ.",
        "Trái Đất không thể tự tạo ra vàng. Hầu hết vàng chúng ta có là do các thiên thạch mang tới sau những vụ va chạm sao Neutron cực lớn.",
        "Các phi hành gia kể rằng không gian có mùi đặc trưng. Nó giống mùi của kim loại nóng, thịt nướng cháy và thuốc súng trộn lại với nhau.",
        "Đỉnh Everest vẫn đang cao thêm khoảng 4mm mỗi năm do mảng kiến tạo Ấn Độ vẫn đang húc vào mảng Á-Âu.",
        "Sinh vật sống lớn nhất Trái Đất là một cây nấm ở Oregon (Mỹ). Hệ thống rễ của nó trải rộng 965 héc-ta và sống hơn 2.400 năm.",
        "Trên Sao Hải Vương và Sao Thiên Vương, áp suất cực lớn có thể khiến carbon bị nén lại và tạo ra những cơn mưa kim cương thực sự.",
        "Loài Gấu nước (Tardigrade) có thể sống sót trong chân không vũ trụ, chịu được phóng xạ và nhiệt độ sôi, là sinh vật 'bất tử' nhất Trái Đất."
    )
    
    // Tin nhắn mặc định khi khởi động cho nhà khoa học
    val defaultScientistMessages = listOf(
        "Bạn thực chất là 'bụi sao'. Các nguyên tử Carbon, Nitơ trong cơ thể bạn được tạo ra từ lò phản ứng hạt nhân của các ngôi sao đã chết hàng tỷ năm trước.",
        "Một ngày trên Sao Kim dài hơn cả một năm trên Sao Kim. Nó quay quanh trục mất 243 ngày Trái Đất, nhưng quay quanh Mặt Trời chỉ mất 225 ngày."
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
     * Lấy câu trả lời tiếp theo dựa trên index cho Bác sĩ
     * Index sẽ được lưu trong database hoặc state
     */
    fun getNextDoctorReply(replyIndex: Int): String {
        return if (replyIndex < doctorReplies.size) {
            doctorReplies[replyIndex]
        } else {
            // Nếu hết câu trả lời, quay lại từ đầu
            doctorReplies[replyIndex % doctorReplies.size]
        }
    }
    
    /**
     * Lấy câu trả lời tiếp theo dựa trên index cho Nhà khoa học
     * Index sẽ được lưu trong database hoặc state
     */
    fun getNextScientistReply(replyIndex: Int): String {
        return if (replyIndex < scientistReplies.size) {
            scientistReplies[replyIndex]
        } else {
            // Nếu hết câu trả lời, quay lại từ đầu
            scientistReplies[replyIndex % scientistReplies.size]
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
     * Lấy tổng số câu trả lời cho Bác sĩ
     */
    fun getTotalDoctorReplies(): Int = doctorReplies.size
    
    /**
     * Lấy tổng số câu trả lời cho Nhà khoa học
     */
    fun getTotalScientistReplies(): Int = scientistReplies.size
    
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

