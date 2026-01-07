package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.navigation.NavController
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    MainContainer(navController = navController) {
        SettingsScreenContent(navController = navController)
    }
}

@Composable
fun SettingsScreenContent(navController: NavController) {
    val context = LocalContext.current
    val settingsDataSource = remember { FakeCallSettingsDataSource(context) }
    val scope = rememberCoroutineScope()
    val systemIsDark = isSystemInDarkTheme()
    
    // Convert Flow thành StateFlow với stateIn để tránh màn hình trắng
    // StateFlow có giá trị initial ngay lập tức, không cần chờ Flow emit
    val themeModeStateFlow: StateFlow<String> = remember(settingsDataSource) {
        settingsDataSource.themeMode.stateIn(
            scope = scope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )
    }
    val themeMode by themeModeStateFlow.collectAsState()
    val isDarkTheme = when (themeMode) {
        ThemeMode.DARK.value -> true
        ThemeMode.LIGHT.value -> false
        else -> systemIsDark
    }

    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
            .padding(16.dp)
    ) {
        // Top bar with title
        Text(
            text = "Cài đặt",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Theme Selection Card + Canvas Switch
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Giao diện tối",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkTheme) "Đang bật chế độ ban đêm" else "Đang bật chế độ ban ngày",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    ThemeSwitch(
                        isDarkTheme = isDarkTheme,
                        onToggle = {
                            scope.launch {
                                val newMode =
                                    if (isDarkTheme) ThemeMode.LIGHT.value else ThemeMode.DARK.value
                                settingsDataSource.setThemeMode(newMode)
                            }
                        },
                        width = 70.dp,
                        height = 36.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeSwitch(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 60.dp,
    height: Dp = 32.dp
) {
    // 1. Animation vị trí của nút tròn (thumb)
    val thumbOffsetAnim by animateDpAsState(
        targetValue = if (isDarkTheme) width - height else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbOffset"
    )

    // 2. Animation màu nền (track)
    val trackColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFF64B5F6),
        animationSpec = tween(500),
        label = "trackColor"
    )

    // 3. Animation màu nút tròn (mặt trời / mặt trăng)
    val thumbColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFFFFEB3B),
        animationSpec = tween(500),
        label = "thumbColor"
    )

    // 4. Animation alpha của sao
    val starAlphaAnim by animateFloatAsState(
        targetValue = if (isDarkTheme) 1f else 0f,
        animationSpec = tween(500),
        label = "starAlpha"
    )

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        val trackCornerRadius = size.height / 2
        val thumbRadius = (size.height / 2) - 4.dp.toPx()

        // Nền (track)
        drawRoundRect(
            color = trackColorAnim,
            cornerRadius = CornerRadius(trackCornerRadius, trackCornerRadius),
            style = Fill
        )

        // Sao nhỏ lấp lánh
        if (starAlphaAnim > 0f) {
            val starColor = Color.White.copy(alpha = starAlphaAnim)
            drawSparkle(
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                size = 4.dp.toPx(),
                color = starColor
            )
            drawSparkle(
                center = Offset(size.width * 0.4f, size.height * 0.7f),
                size = 3.dp.toPx(),
                color = starColor
            )
            drawSparkle(
                center = Offset(size.width * 0.5f, size.height * 0.2f),
                size = 4.dp.toPx(),
                color = starColor
            )
        }

        // Nút tròn (thumb)
        val thumbCenterX = thumbOffsetAnim.toPx() + (size.height / 2)
        val thumbCenterY = size.height / 2
        val thumbCenterOffset = Offset(thumbCenterX, thumbCenterY)

        if (isDarkTheme) {
            // Vẽ mặt trăng khuyết
            drawCrescentMoon(
                center = Offset(thumbCenterX, thumbCenterY),
                radius = thumbRadius * 0.8f,
                color = Color(0xFFFFF9C4)
            )
        } else {
            // Vẽ mặt trời với tia nắng
            val coreRad = thumbRadius * 0.6f
            val rayLen = thumbRadius * 0.3f
            val rayWid = thumbRadius * 0.1f
            val gap = thumbRadius * 0.1f

            drawSun(
                center = thumbCenterOffset,
                coreRadius = coreRad,
                rayLength = rayLen,
                rayWidth = rayWid,
                rayGap = gap,
                color = Color(0xFFFFEB3B), // Vàng tươi
                numRays = 8,
                rotationDegrees = 0f
            )
        }
    }
}

// --- HÀM VẼ MẶT TRĂNG KHUYẾT (Dùng kỹ thuật cắt hình) ---
fun DrawScope.drawCrescentMoon(
    center: Offset,
    radius: Float,
    color: Color
) {
    // Hình tròn gốc (Mặt trăng đầy)
    val circleA = Path().apply {
        addOval(Rect(center = center, radius = radius))
    }

    // Hình tròn dùng để cắt (Lệch sang phải và lên trên một chút)
    val circleB = Path().apply {
        addOval(
            Rect(
                center = Offset(
                    x = center.x + radius * 0.4f, // Lệch phải 40%
                    y = center.y - radius * 0.1f  // Lên trên 10%
                ),
                radius = radius // Cùng kích thước
            )
        )
    }

    // Thực hiện phép trừ: A - B
    val moonPath = Path.combine(
        operation = PathOperation.Difference,
        path1 = circleA,
        path2 = circleB
    )

    drawPath(path = moonPath, color = color)
}

// --- HÀM VẼ NGÔI SAO 5 CÁNH (Dùng toán học) ---
fun DrawScope.drawStar(
    center: Offset,
    outerRadius: Float, // Đỉnh nhọn
    innerRadius: Float, // Điểm lõm
    color: Color,
    numPoints: Int = 5
) {
    val path = Path()
    val theta = 2.0 * PI / numPoints // Góc giữa các cánh

    // Bắt đầu vẽ
    // Công thức tính tọa độ: 
    // x = center.x + radius * cos(angle)
    // y = center.y + radius * sin(angle)
    // Lưu ý: -PI/2 để bắt đầu từ đỉnh cao nhất (12 giờ)
    
    for (i in 0 until numPoints) {
        val angleOuter = -PI / 2 + i * theta
        val angleInner = angleOuter + theta / 2

        // Điểm đỉnh nhọn
        val xOuter = center.x + outerRadius * cos(angleOuter).toFloat()
        val yOuter = center.y + outerRadius * sin(angleOuter).toFloat()

        // Điểm lõm vào
        val xInner = center.x + innerRadius * cos(angleInner).toFloat()
        val yInner = center.y + innerRadius * sin(angleInner).toFloat()

        if (i == 0) {
            path.moveTo(xOuter, yOuter)
        } else {
            path.lineTo(xOuter, yOuter)
        }
        path.lineTo(xInner, yInner)
    }
    path.close() // Nối điểm cuối về điểm đầu

    drawPath(path = path, color = color)
}

// --- HÀM VẼ NGÔI SAO 4 CÁNH (Dạng lấp lánh đơn giản) ---
fun DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - size) // Đỉnh trên
        quadraticBezierTo(center.x, center.y, center.x + size, center.y) // Cong sang phải
        quadraticBezierTo(center.x, center.y, center.x, center.y + size) // Cong xuống dưới
        quadraticBezierTo(center.x, center.y, center.x - size, center.y) // Cong sang trái
        quadraticBezierTo(center.x, center.y, center.x, center.y - size) // Cong về đỉnh
        close()
    }
    drawPath(path = path, color = color)
}

// --- HÀM VẼ MẶT TRỜI ---
fun DrawScope.drawSun(
    center: Offset,
    coreRadius: Float,   // Bán kính tâm mặt trời
    rayLength: Float,    // Độ dài tia nắng
    rayWidth: Float,     // Độ dày tia nắng
    rayGap: Float = 5f,  // Khoảng cách từ tâm đến chân tia nắng
    color: Color,
    numRays: Int = 8,    // Số lượng tia nắng (thường là 8 hoặc 12)
    rotationDegrees: Float = 0f // Góc xoay tổng thể (để làm animation)
) {
    // 1. Nếu muốn cả mặt trời xoay (animation), ta xoay toàn bộ canvas trước
    rotate(degrees = rotationDegrees, pivot = center) {

        // 2. Vẽ hình tròn trung tâm (Lõi mặt trời)
        drawCircle(
            color = color,
            radius = coreRadius,
            center = center
        )

        // 3. Vẽ các tia nắng xung quanh
        val angleStep = 360f / numRays // Góc giữa mỗi tia (VD: 8 tia thì cách nhau 45 độ)
        val startDistance = coreRadius + rayGap // Điểm bắt đầu vẽ tia (tính từ tâm)
        val endDistance = startDistance + rayLength // Điểm kết thúc tia

        repeat(numRays) { index ->
            // Mẹo quan trọng: Xoay canvas để vẽ tia tiếp theo
            // Ta xoay quanh điểm center một góc là (index * angleStep)
            rotate(degrees = index * angleStep, pivot = center) {
                // Sau khi xoay, ta chỉ cần vẽ một đường thẳng đứng đơn giản
                // hướng từ tâm lên trên (theo trục Y âm)
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - startDistance),
                    end = Offset(center.x, center.y - endDistance),
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round // Bo tròn đầu tia nắng cho mềm mại
                )
            }
        }
    }
}

