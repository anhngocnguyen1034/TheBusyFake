package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.os.Build
import java.util.Locale

object AutoReplyHelper {

    // --- Locale detection ---
    private fun currentLanguage(context: Context): String {
        val config = context.resources.configuration
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
        return locale.language
    }

    private fun isVietnamese(context: Context): Boolean = currentLanguage(context) == "vi"

    // --- Mẹ / Mom ---
    private val momRepliesVi = listOf(
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

    private val momRepliesEn = listOf(
        "Hey, I'm a bit busy right now. Do you need anything?",
        "I miss you. Have you eaten yet?",
        "Come home early, I cooked your favorite dish.",
        "I'm out shopping. Do you want me to buy anything for you?",
        "Have you finished studying? Don't stay up too late.",
        "You look a bit tired lately. Are you okay?",
        "Remember to drink enough water. Your health matters.",
        "I'm cooking dinner. Come home and eat with me.",
        "Do you remember what day it is today? I'm thinking of you.",
        "I'm thinking about you. Do you want to talk for a bit?",
        "Don't forget to dress warmly, it's getting cold.",
        "I just baked something. Come home and try it.",
        "Are your friends coming over? I'll prepare some food.",
        "I know you're working hard. Try your best but don't overdo it.",
        "Call me when you get home, so I won't worry.",
        "I'm tidying up the house. Come help me when you're free.",
        "Is there anything special you'd like me to cook for you?",
        "I miss when you were little. Time flies so fast.",
        "Don't forget to exercise a bit. Health is golden.",
        "I love you so much. You make me proud every day."
    )

    private fun momReplies(context: Context): List<String> =
        if (isVietnamese(context)) momRepliesVi else momRepliesEn

    private val defaultMomMessagesVi = listOf(
        "Con ơi, mẹ nhớ con lắm. Con có khỏe không?",
        "Mẹ vừa nấu món con thích đấy. Con về nhà sớm nhé.",
        "Con đừng quên ăn đủ bữa nhé. Mẹ lo lắng lắm."
    )

    private val defaultMomMessagesEn = listOf(
        "Hey, I miss you. How are you doing?",
        "I just cooked your favorite dish. Come home early, okay?",
        "Don't skip your meals, I worry about you."
    )

    fun defaultMomMessages(context: Context): List<String> =
        if (isVietnamese(context)) defaultMomMessagesVi else defaultMomMessagesEn

    // --- Người yêu / Lover ---
    private val loverRepliesVi = listOf(
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

    private val loverRepliesEn = listOf(
        "I really miss you. What are you up to?",
        "Are you free today? Let's go out together.",
        "I was just thinking about you. Do you miss me too?",
        "I love you so much, you know that?",
        "I'm feeling a bit down today. Can we talk for a while?",
        "I want to see you. When can we meet?",
        "What are you doing right now? I miss you a lot.",
        "I just watched a great movie. Let's watch it together sometime.",
        "Are you hungry? I can cook your favorite food.",
        "You look so good today. I really like it.",
        "Do you want to grab a coffee? I'd really love to see you.",
        "I'm listening to sad songs... can you stay with me for a bit?",
        "You are the most important person to me. I love you.",
        "I just bought you a gift. Want a hint?",
        "I'm happy today because of you. Thank you.",
        "Do you remember the first time we met? I still do.",
        "I want to be by your side forever. Would you like that?",
        "Don't skip your meals, I worry about you.",
        "You seem a bit tired today. Are you okay?",
        "You're the reason I wake up with a smile every morning."
    )

    private fun loverReplies(context: Context): List<String> =
        if (isVietnamese(context)) loverRepliesVi else loverRepliesEn

    private val defaultLoverMessagesVi = listOf(
        "Mình nhớ bạn lắm. Bạn có khỏe không?",
        "Hôm nay mình muốn gặp bạn. Mình đi chơi nhé.",
        "Mình yêu bạn nhiều lắm. Bạn biết không?"
    )

    private val defaultLoverMessagesEn = listOf(
        "I miss you. How have you been?",
        "I really want to see you today. Let's go out.",
        "I love you so much. You know that, right?"
    )

    fun defaultLoverMessages(context: Context): List<String> =
        if (isVietnamese(context)) defaultLoverMessagesVi else defaultLoverMessagesEn

    // --- Bác sĩ / Doctor ---
    private val doctorRepliesVi = listOf(
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

    private val doctorRepliesEn = listOf(
        "Have you had enough water today? Even mild dehydration can affect memory and focus.",
        "If you're staring at a screen, try the 20-20-20 rule: every 20 minutes, look 20 feet away for 20 seconds.",
        "Try this breathing exercise: inhale 4 seconds, hold 7, exhale 8. It calms your nervous system.",
        "Are you slouching right now? Bad posture reduces oxygen to your brain. Stand up and stretch a bit.",
        "Chronic stress raises cortisol, which can hurt your immune system. Relax your shoulders and breathe.",
        "Try to sleep before 11 PM. Your brain needs deep sleep to 'clean up' the toxins of the day.",
        "Breakfast jump-starts your metabolism. Skipping it is like running a car without fuel.",
        "Avoid too much sugar in the afternoon; the crash will make you even more tired.",
        "Your body is built to move. Walk around a bit every hour to protect your heart.",
        "Limit phone use an hour before bed. Blue light makes it harder to fall into deep sleep.",
        "If you can, get some morning sunlight. It boosts Vitamin D and your mood.",
        "Your gut is your 'second brain'. Eat some fiber or yogurt to keep it happy.",
        "Avoid coffee too late in the day. Caffeine can stay in your system for hours.",
        "Sometimes the best medicine is turning off notifications for a while.",
        "Don't ignore small pains; they're your body's way of asking for a break.",
        "Try to find one small thing that makes you smile every day.",
        "A slightly cool room actually helps you fall asleep faster and deeper.",
        "Work will always be there; your health won't. Take care of yourself first.",
        "Remember to blink more when focused on screens to avoid dry eyes.",
        "A simple smile can relax your face and lower stress hormones."
    )

    private fun doctorReplies(context: Context): List<String> =
        if (isVietnamese(context)) doctorRepliesVi else doctorRepliesEn

    private val defaultDoctorMessagesVi = listOf(
        "Bạn nhớ kiểm tra xem đã uống đủ nước chưa? Não bộ chứa khoảng 73% là nước, mất nước nhẹ cũng làm giảm trí nhớ và sự tập trung đấy.",
        "Nếu bạn đang nhìn màn hình lâu, hãy áp dụng quy tắc 20-20-20: Cứ 20 phút, nhìn xa 6 mét trong 20 giây để giảm cận thị giả."
    )

    private val defaultDoctorMessagesEn = listOf(
        "Have you checked if you've had enough water today? Your brain needs it to focus.",
        "If you stare at screens a lot, try the 20-20-20 rule to rest your eyes."
    )

    fun defaultDoctorMessages(context: Context): List<String> =
        if (isVietnamese(context)) defaultDoctorMessagesVi else defaultDoctorMessagesEn

    // --- Nhà khoa học / Scientist ---
    private val scientistRepliesVi = listOf(
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

    private val scientistRepliesEn = listOf(
        "You are literally made of stardust. The atoms in your body were forged in ancient stars.",
        "A day on Venus is longer than a year there. It spins once every 243 Earth days but orbits the Sun in 225.",
        "When you look at the stars, you're looking into the past. Sunlight takes about 8 minutes to reach Earth.",
        "If you stretched all the DNA in your body in a line, it would reach far beyond Pluto and back many times.",
        "Atoms are mostly empty space. If you removed the empty space from all humans, we'd fit into a sugar cube.",
        "Octopuses have three hearts, nine brains, and blue blood due to copper instead of iron.",
        "Sharks are older than trees. They appeared around 400 million years ago; trees, about 350 million.",
        "Cleopatra lived closer in time to the invention of the iPhone than to the building of the Great Pyramid.",
        "A fluffy white cloud can weigh around 500 tons, yet floats because the air beneath it is even heavier.",
        "Bananas are slightly radioactive due to potassium-40—but you'd need millions at once to be in danger.",
        "A teaspoon of neutron star material would weigh billions of tons on Earth.",
        "Your body hosts more bacterial cells than human cells—you are basically a walking ecosystem.",
        "Some of the water molecules you drink are older than the Sun itself.",
        "Your brain awake can power a small LED bulb with its electrical activity.",
        "Most gold on Earth arrived via ancient cosmic collisions and meteorites, not from our planet.",
        "Astronauts say space smells like hot metal, seared steak, and welding fumes on their suits.",
        "Mount Everest is still growing a few millimeters each year as tectonic plates push together.",
        "The largest living organism is a fungus in Oregon, spreading across hundreds of hectares.",
        "On Uranus and Neptune, extreme pressure may create real diamond rain.",
        "Tardigrades can survive in space, high radiation, and boiling or freezing conditions."
    )

    private fun scientistReplies(context: Context): List<String> =
        if (isVietnamese(context)) scientistRepliesVi else scientistRepliesEn

    private val defaultScientistMessagesVi = listOf(
        "Bạn thực chất là 'bụi sao'. Các nguyên tử Carbon, Nitơ trong cơ thể bạn được tạo ra từ lò phản ứng hạt nhân của các ngôi sao đã chết hàng tỷ năm trước.",
        "Một ngày trên Sao Kim dài hơn cả một năm trên Sao Kim. Nó quay quanh trục mất 243 ngày Trái Đất, nhưng quay quanh Mặt Trời chỉ mất 225 ngày."
    )

    private val defaultScientistMessagesEn = listOf(
        "You are literally made of stardust. The atoms in your body came from ancient stars.",
        "A day on Venus is longer than its year—it spins slower than it orbits the Sun."
    )

    fun defaultScientistMessages(context: Context): List<String> =
        if (isVietnamese(context)) defaultScientistMessagesVi else defaultScientistMessagesEn

    // --- Generic replies ---
    private val genericRepliesVi = listOf(
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

    private val genericRepliesEn = listOf(
        "Hey, I just saw your message. What are you up to?",
        "Thanks for texting. I'm a bit busy, I'll reply properly in a moment.",
        "I saw your message. Do you need anything?",
        "Hi! I'm here and listening. What would you like to say?",
        "Just read your message. How are you feeling today?",
        "Thanks for reaching out. I'll get back to you soon.",
        "I saw your message. How's your day going?",
        "Hey, I'm here. Go ahead, I'm listening.",
        "I just read your message. Do you want to meet up?",
        "Thanks, I'm thinking about what you said.",
        "I saw your text. Are you free right now?",
        "Hi! I'm around. Do you need anything?",
        "I just checked your message. Want to chat for a bit?",
        "Thanks for your message. I'll reply in a second.",
        "Got your message. Where are you now?",
        "Hey, I'm listening. Keep talking.",
        "Just read your message. Hope you're doing okay.",
        "Thanks, I'm considering it.",
        "I saw your message. What would you like to do?",
        "Hey, I'm here. Tell me more."
    )

    private fun genericReplies(context: Context): List<String> =
        if (isVietnamese(context)) genericRepliesVi else genericRepliesEn
    
    // --- Public APIs used bởi ViewModel (đã locale-aware) ---

    fun getNextReply(context: Context, replyIndex: Int): String {
        val list = momReplies(context)
        return list[replyIndex % list.size]
    }

    fun getNextMomReply(context: Context, replyIndex: Int): String = getNextReply(context, replyIndex)

    fun getNextLoverReply(context: Context, replyIndex: Int): String {
        val list = loverReplies(context)
        return list[replyIndex % list.size]
    }

    fun getNextDoctorReply(context: Context, replyIndex: Int): String {
        val list = doctorReplies(context)
        return list[replyIndex % list.size]
    }

    fun getNextScientistReply(context: Context, replyIndex: Int): String {
        val list = scientistReplies(context)
        return list[replyIndex % list.size]
    }

    fun getTotalReplies(context: Context): Int = momReplies(context).size

    fun getTotalLoverReplies(context: Context): Int = loverReplies(context).size

    fun getTotalDoctorReplies(context: Context): Int = doctorReplies(context).size

    fun getTotalScientistReplies(context: Context): Int = scientistReplies(context).size

    fun getNextGenericReply(context: Context, replyIndex: Int): String {
        val list = genericReplies(context)
        return list[replyIndex % list.size]
    }

    fun getTotalGenericReplies(context: Context): Int = genericReplies(context).size
}

