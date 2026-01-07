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
- **SYSTEM_ALERT_WINDOW**: Để hiển thị overlay màn hình
- **SCHEDULE_EXACT_ALARM**: Để lên lịch chính xác thời gian cuộc gọi
- **USE_EXACT_ALARM**: Để sử dụng alarm chính xác

**Lưu ý**: Trên Android 6.0+ (API 23+), bạn cần cấp quyền "Display over other apps" thủ công trong Settings.

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
5. `FakeCallReceiver` start `FakeCallService`
6. `FakeCallService` hiển thị overlay màn hình với Compose UI
7. User nhấn Answer hoặc Decline để đóng overlay

## Lưu ý kỹ thuật

- Alarm được lưu trong memory (có thể thay bằng Room/SharedPreferences)
- Overlay sử dụng `WindowManager` với `TYPE_APPLICATION_OVERLAY`
- Service chạy foreground để đảm bảo overlay hiển thị
- Cần cấp quyền overlay thủ công trên Android 6.0+






