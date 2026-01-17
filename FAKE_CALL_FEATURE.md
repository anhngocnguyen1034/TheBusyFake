# Tính năng Fake Call (Cuộc gọi giả)

## Tổng quan
Tính năng này cho phép bạn lên lịch các cuộc gọi giả sẽ xuất hiện sau một khoảng thời gian nhất định. Khi đến thời gian đã đặt, một overlay màn hình sẽ hiển thị như một cuộc gọi thật.

## Cách sử dụng

### 1. Lên lịch cuộc gọi giả
- Nhập **Tên người gọi** (ví dụ: "John Doe")
- Nhập **Số điện thoại** (ví dụ: "+84 123 456 789")
- Nhập **Thời gian chờ** (tính bằng phút, ví dụ: "5" nghĩa là sau 5 phút)
- Nhấn nút **"Schedule Call"**

### 2. Xem danh sách cuộc gọi đã lên lịch
- Danh sách các cuộc gọi đã lên lịch sẽ hiển thị bên dưới form
- Mỗi item hiển thị:
  - Tên người gọi
  - Số điện thoại
  - Thời gian đã lên lịch
  - Thời gian còn lại (hoặc "Overdue" nếu đã quá thời gian)

### 3. Hủy cuộc gọi đã lên lịch
- Nhấn vào icon **Xóa** (Delete) bên cạnh cuộc gọi muốn hủy

### 4. Khi cuộc gọi đến
- Khi đến thời gian đã đặt, một overlay màn hình sẽ xuất hiện
- Hiển thị:
  - Tên người gọi
  - Số điện thoại
  - 2 nút: **Trả lời** (màu xanh) và **Từ chối** (màu đỏ)
- Nhấn bất kỳ nút nào để đóng overlay

## Quyền cần thiết

Ứng dụng cần các quyền sau để hoạt động:
- **SCHEDULE_EXACT_ALARM**: Để lên lịch chính xác thời gian cuộc gọi (tuân thủ Google Play Policy)
- **USE_FULL_SCREEN_INTENT**: Để hiển thị cuộc gọi toàn màn hình khi màn hình tắt (tính năng cốt lõi, được Google Play chấp nhận)

**Lưu ý quan trọng về Google Play Policy:**
- ❌ **KHÔNG sử dụng USE_EXACT_ALARM**: Chỉ dành cho ứng dụng Đồng hồ báo thức hoặc Lịch
- ❌ **KHÔNG sử dụng SYSTEM_ALERT_WINDOW**: Không cần thiết vì app dùng Full Screen Intent thông qua Notification (an toàn hơn)
- ✅ **SCHEDULE_EXACT_ALARM**: Được phép cho Fake Call app
- ✅ **USE_FULL_SCREEN_INTENT**: Được phép vì đây là tính năng cốt lõi của app

## Kiến trúc

Tính năng này được xây dựng theo Clean Architecture:

### Domain Layer
- `FakeCall` - Entity đại diện cho cuộc gọi giả
- `FakeCallRepository` - Interface repository
- `ScheduleFakeCallUseCase`, `CancelFakeCallUseCase`, `GetScheduledCallsUseCase` - Use cases
- `CallScheduler` - Interface để lên lịch alarm

### Data Layer
- `FakeCallData` - Data model
- `FakeCallMapper` - Mapper giữa Data và Domain
- `LocalFakeCallDataSource` - Data source lưu trữ local
- `FakeCallRepositoryImpl` - Implementation của repository

### Presentation Layer
- `FakeCallViewModel` - ViewModel quản lý state
- `FakeCallScreen` - UI screen
- `FakeCallService` - Service hiển thị overlay
- `FakeCallReceiver` - BroadcastReceiver nhận alarm
- `AlarmSchedulerImpl` - Implementation của CallScheduler

## Luồng hoạt động

1. User nhập thông tin và nhấn "Schedule Call"
2. ViewModel gọi `ScheduleFakeCallUseCase` để lưu vào repository
3. ViewModel gọi `CallScheduler.schedule()` để đặt alarm
4. Khi đến thời gian, `AlarmManager` trigger `FakeCallReceiver`
5. `FakeCallReceiver` kiểm tra trạng thái app:
   - **Nếu app đang chạy**: Hiển thị Activity trực tiếp
   - **Nếu app đã đóng**: Hiển thị Notification với Full Screen Intent (an toàn với Google Play)
6. Full Screen Intent tự động mở `FakeCallActivity` khi màn hình tắt (giống cuộc gọi thật)
7. User nhấn Answer hoặc Decline để đóng Activity

## Lưu ý kỹ thuật

- Alarm được lưu trong memory (có thể thay bằng Room/SharedPreferences)
- **Không sử dụng Overlay**: App sử dụng Full Screen Intent thông qua Notification (tuân thủ Google Play Policy)
- Notification với Full Screen Intent tự động mở Activity khi màn hình tắt
- Activity sử dụng `showWhenLocked` và `turnScreenOn` để hiển thị trên màn hình khóa
- Không cần quyền SYSTEM_ALERT_WINDOW (đã loại bỏ để tránh bị Google Play từ chối)






