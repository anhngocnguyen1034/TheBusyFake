# Google Play Policy Compliance - Fake Call App

## ✅ Đã Tuân Thủ Chính Sách Google Play

### 1. Quyền Alarm (Alarm Permissions)

#### ❌ KHÔNG SỬ DỤNG: `USE_EXACT_ALARM`
- **Lý do**: Chỉ dành cho ứng dụng Đồng hồ báo thức (Alarm Clock) hoặc Lịch (Calendar)
- **Rủi ro**: Google sẽ Reject ngay lập tức nếu app Fake Call xin quyền này
- **Trạng thái**: ✅ **Đã loại bỏ hoàn toàn**

#### ✅ ĐANG SỬ DỤNG: `SCHEDULE_EXACT_ALARM`
- **Lý do**: Quyền này được phép cho các app cần lên lịch chính xác (như Fake Call)
- **Trạng thái**: ✅ **Đã khai báo trong AndroidManifest.xml**

### 2. Quyền Hiển Thị (Display Permissions)

#### ❌ KHÔNG SỬ DỤNG: `SYSTEM_ALERT_WINDOW` (Display over other apps)
- **Lý do từ chối**:
  1. **Google Play Policy**: Quyền này rất dễ bị lợi dụng để che màn hình, lừa đảo (clickjacking)
  2. **UX cực tệ**: Người dùng phải vào Settings thủ công, không có popup đơn giản
  3. **Tỷ lệ gỡ app cao**: Người dùng sợ bị virus khi app đòi vào sâu trong Settings
  4. **Có giải pháp tốt hơn**: `USE_FULL_SCREEN_INTENT` là cách chuẩn cho cuộc gọi
- **Trạng thái**: ✅ **Đã loại bỏ hoàn toàn khỏi AndroidManifest.xml**

#### ✅ ĐANG SỬ DỤNG: `USE_FULL_SCREEN_INTENT`
- **Lý do**: Đây là quyền chuẩn cho các app gọi điện (như Zalo, Messenger)
- **Hành vi**:
  - Khi màn hình tắt: Tự động bật sáng và hiện Activity full màn (giống cuộc gọi thật)
  - Khi đang dùng app khác: Hiện thông báo dạng Heads-up (banner thả xuống từ trên)
- **Google Play**: ✅ **Được chấp nhận vì đây là tính năng cốt lõi của app**
- **Trạng thái**: ✅ **Đã khai báo trong AndroidManifest.xml**

### 3. Kiến Trúc Hiển Thị Fake Call

#### ✅ Cách Làm Chuẩn (Đang Sử Dụng)

**Khi app đang chạy:**
- Hiển thị Activity trực tiếp (không cần notification)

**Khi app đã đóng:**
- Sử dụng Notification với Full Screen Intent
- Full Screen Intent tự động mở `FakeCallActivity` khi màn hình tắt
- Activity sử dụng `showWhenLocked` và `turnScreenOn` để hiển thị trên màn hình khóa

**Lợi ích:**
- ✅ Tuân thủ Google Play Policy
- ✅ Trải nghiệm giống cuộc gọi thật
- ✅ Không cần quyền đặc biệt từ người dùng
- ✅ UX tốt hơn (không phải vào Settings)

#### ❌ Cách Làm Sai (Đã Loại Bỏ)

**Dùng SYSTEM_ALERT_WINDOW:**
- Vẽ view đè lên màn hình
- Có thể không che hết Status bar hoặc Navigation bar
- Nhìn "giả" và thiếu chuyên nghiệp
- Bị Google Play từ chối

### 4. Code Changes

#### AndroidManifest.xml
```xml
<!-- ✅ ĐÚNG -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />

<!-- ❌ ĐÃ XÓA -->
<!-- <uses-permission android:name="android.permission.USE_EXACT_ALARM" /> -->
<!-- <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" /> -->
```

#### PermissionHelper.kt
- ✅ Đã đánh dấu `@Deprecated` các function liên quan đến overlay
- ✅ Function `canDrawOverlays()` luôn trả về `false`
- ✅ Function `requestOverlayPermission()` không làm gì

#### MainActivity.kt
- ✅ Đã comment code auto-request overlay permission
- ⚠️ Vẫn giữ lại code structure để tương thích (nhưng không được gọi)

#### FakeCallReceiver.kt
- ✅ Sử dụng Full Screen Intent thông qua Notification
- ✅ Không sử dụng overlay window

### 5. Documentation

#### FAKE_CALL_FEATURE.md
- ✅ Đã cập nhật thông tin về quyền
- ✅ Giải thích tại sao dùng Full Screen Intent
- ✅ Thêm lưu ý về Google Play Policy

### 6. Kết Luận

**App hiện tại:**
- ✅ **Hoàn toàn tuân thủ** Google Play Policy
- ✅ **Không có quyền nguy hiểm** (USE_EXACT_ALARM)
- ✅ **Không có quyền không cần thiết** (SYSTEM_ALERT_WINDOW)
- ✅ **Sử dụng giải pháp chuẩn** (Full Screen Intent)
- ✅ **Sẵn sàng submit** lên Google Play Store

**Lưu ý khi submit:**
- Khi Google hỏi về `USE_FULL_SCREEN_INTENT`, giải trình:
  > "App mô phỏng cuộc gọi đến, cần hiển thị toàn màn hình như một cuộc gọi thật để người dùng trải nghiệm. Đây là tính năng cốt lõi của app."

---

**Ngày cập nhật**: 2024-2025  
**Trạng thái**: ✅ Compliant với Google Play Policy
