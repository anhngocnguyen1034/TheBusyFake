package com.example.thebusysimulator.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

// Local để truyền trạng thái theme xuống các composable con
val LocalThemeIsDark = staticCompositionLocalOf { false }

/**
 * Composable wrapper để thêm hiệu ứng transition khi đổi theme
 * @param isDarkTheme: Trạng thái theme hiện tại
 * @param content: Nội dung UI với function trigger animation
 */
@Composable
fun ThemeTransitionWrapper(
    isDarkTheme: Boolean,
    content: @Composable (triggerAnimation: (Offset) -> Unit) -> Unit
) {
    // Các biến phục vụ animation
    val scope = rememberCoroutineScope()
    // Giá trị animation từ 0f (bắt đầu) -> 1f (kết thúc)
    val revealProgress = remember { Animatable(1f) }
    // Tọa độ tâm điểm nơi bắt đầu hiệu ứng lan tỏa
    var revealCenter by remember { mutableStateOf(Offset.Zero) }
    // Lưu lại màu nền cũ để lớp Canvas phủ lên
    var previousBackgroundColor by remember { mutableStateOf(Color.Transparent) }
    // Biến cờ để biết animation có đang chạy không
    var isAnimating by remember { mutableStateOf(false) }
    // Track theme cũ để phát hiện thay đổi
    var previousIsDark by remember { mutableStateOf(isDarkTheme) }

    // Xác định màu nền hiện tại
    val currentThemeBgColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF0F0F0)

    // Phát hiện khi theme thay đổi và trigger animation
    LaunchedEffect(isDarkTheme) {
        if (isDarkTheme != previousIsDark && !isAnimating) {
            // Theme đã thay đổi, nhưng chưa có animation được trigger
            // Animation sẽ được trigger từ onClick
        }
        previousIsDark = isDarkTheme
    }

    // Hàm kích hoạt animation - dùng remember để stable reference
    val triggerAnimation: (Offset) -> Unit = remember {
        fun(clickOffset: Offset) {
            if (isAnimating) return // Tránh spam click

            scope.launch {
                isAnimating = true
                revealCenter = clickOffset
                // Quan trọng: Lưu màu nền CŨ (dựa trên isDarkTheme hiện tại, vì animation được trigger TRƯỚC KHI đổi theme)
                previousBackgroundColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF0F0F0)
                // Đặt progress về 0 để bắt đầu
                revealProgress.snapTo(0f)
                // Chạy animation đến 1f
                revealProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                )
                isAnimating = false
            }
        }
    }

    CompositionLocalProvider(LocalThemeIsDark provides isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- LỚP 1: Giao diện thực tế (Real UI) ---
            content(triggerAnimation)

            // --- LỚP 2: Lớp phủ Canvas (Overlay) ---
            // Chỉ hiện khi đang animation và chưa kết thúc (progress < 1f)
            if (isAnimating && revealProgress.value < 1f) {
                CanvasRevealOverlay(
                    progress = revealProgress.value,
                    centerOffset = revealCenter,
                    overlayColor = previousBackgroundColor
                )
            }
        }
    }
}

// --- Composable vẽ lớp phủ hiệu ứng bằng Canvas ---
@Composable
fun CanvasRevealOverlay(
    progress: Float,
    centerOffset: Offset,
    overlayColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // Đảm bảo lớp này nằm trên cùng và chặn sự kiện chạm khi đang animation
            .pointerInput(Unit) {}
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Tính toán bán kính tối đa cần thiết để phủ kín màn hình từ điểm chạm
        // Dùng công thức Pytago để tính đường chéo xa nhất
        val maxRadius = hypot(
            max(centerOffset.x, canvasWidth - centerOffset.x),
            max(centerOffset.y, canvasHeight - centerOffset.y)
        )

        // Bán kính hiện tại dựa trên tiến độ animation
        val currentRadius = maxRadius * progress

        // --- KỸ THUẬT CHÍNH: ClipOp.Difference ---
        // 1. Tạo một đường dẫn hình tròn (cái lỗ sẽ mở ra)
        val circlePath = Path().apply {
            addOval(Rect(center = centerOffset, radius = currentRadius))
        }

        // 2. Yêu cầu Canvas chỉ vẽ vào phần NẰM NGOÀI hình tròn (Difference)
        clipPath(path = circlePath, clipOp = ClipOp.Difference) {
            // 3. Vẽ màu nền cũ lên toàn bộ phần nằm ngoài hình tròn đó
            drawRect(color = overlayColor)
        }
        // Kết quả: Ta thấy giao diện mới bên trong hình tròn, và màu cũ bên ngoài.
    }
}

/**
 * Helper để lấy vị trí click từ một composable
 */
@Composable
fun rememberClickPosition(
    onPositionReady: (Offset) -> Unit
): Modifier {
    var position by remember { mutableStateOf<Offset?>(null) }
    
    return Modifier
        .onGloballyPositioned { coordinates ->
            position = coordinates.positionInRoot() + Offset(
                coordinates.size.width / 2f,
                coordinates.size.height / 2f
            )
        }
        .clickable {
            position?.let { onPositionReady(it) }
        }
}

