package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thebusysimulator.data.datasource.FakeCallSettingsDataSource
import com.example.thebusysimulator.data.datasource.LanguageDataSource
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.GenZTheme
import com.example.thebusysimulator.presentation.ui.theme.ThemeMode
import com.example.thebusysimulator.presentation.util.LanguageManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val theme = getGenZTheme()
    MainContainer(navController = navController, theme = theme) {
        SettingsScreenContent(navController = navController)
    }
}

@Composable
fun SettingsScreenContent(navController: NavController) {
    val context = LocalContext.current
    val settingsDataSource = remember { FakeCallSettingsDataSource(context) }
    val languageDataSource = remember { LanguageDataSource(context) }
    val scope = rememberCoroutineScope()
    val systemIsDark = isSystemInDarkTheme()

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

    var currentLanguageCode by remember { mutableStateOf("en") }
    LaunchedEffect(Unit) {
        languageDataSource.languageCode.collect { code ->
            currentLanguageCode = code
        }
    }
    val currentLanguage = LanguageManager.Language.fromCode(currentLanguageCode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Tăng khoảng cách cho thoáng
    ) {
        // --- HEADER ---
        Text(
            text = "SETTINGS", // Đổi sang tiếng Anh viết hoa cho chuẩn style
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = GenZTheme.colors.text,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // --- THEME SECTION ---
        // Thay Card bằng GenZOptionBox
        GenZOptionBox(
            onClick = {
                // Click vào cả box cũng đổi theme
                scope.launch {
                    val newMode = if (isDarkTheme) ThemeMode.LIGHT.value else ThemeMode.DARK.value
                    settingsDataSource.setThemeMode(newMode)
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "APPEARANCE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GenZTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDarkTheme) "Dark Mode (Hacker)" else "Light Mode (Paper)",
                        style = MaterialTheme.typography.bodySmall,
                        color = GenZTheme.colors.text.copy(alpha = 0.7f)
                    )
                }

                GenZThemeSwitch(
                    isDarkTheme = isDarkTheme,
                    onToggle = {
                        scope.launch {
                            val newMode = if (isDarkTheme) ThemeMode.LIGHT.value else ThemeMode.DARK.value
                            settingsDataSource.setThemeMode(newMode)
                        }
                    }
                )
            }
        }

        // --- LANGUAGE SECTION ---
        GenZOptionBox(
            onClick = { navController.navigate(Screen.Language.route) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LANGUAGE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GenZTheme.colors.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentLanguage?.displayName ?: "English",
                        style = MaterialTheme.typography.bodySmall,
                        color = GenZTheme.colors.text.copy(alpha = 0.7f)
                    )
                }

                // Mũi tên tùy biến
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GenZTheme.colors.neonPink, CircleShape)
                        .border(2.dp, GenZTheme.colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * CONTAINER GEN Z: Thay thế cho Card
 * - Có viền dày (Border)
 * - Có bóng cứng (Hard Shadow)
 * - Hiệu ứng nhấn thụt xuống
 */
@Composable
fun GenZOptionBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation thụt xuống
    val offsetAnim by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        label = "boxOffset"
    )

    // LẤY MÀU RA NGOÀI ĐỂ TRÁNH LỖI (Best Practice)
    val shadowColor = GenZTheme.colors.shadow
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderColor = GenZTheme.colors.border

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(bottom = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Shadow Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(shadowColor, RoundedCornerShape(12.dp)) // Dùng biến đã lấy
        )

        // Content Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetAnim, y = offsetAnim)
                // SỬA LỖI BACKGROUND: Ghi rõ tên tham số color = và shape =
                .background(color = surfaceColor, shape = RoundedCornerShape(12.dp))
                .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * CUSTOM SWITCH THEO STYLE GEN Z
 * - Thêm viền đen cho track
 * - Màu sắc rực rỡ hơn (Neon)
 */
@Composable
fun GenZThemeSwitch(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 64.dp, // To hơn chút cho dễ bấm
    height: Dp = 36.dp
) {
    // Animation Thumb
    val thumbOffsetAnim by animateDpAsState(
        targetValue = if (isDarkTheme) width - height else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "thumb"
    )

    // Màu Track: Dark -> Xám đậm, Light -> Xanh trời
    val trackColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFF2D2D2D) else GenZTheme.colors.neonBlue,
        label = "track"
    )

    // Màu Thumb: Dark -> Trắng/Xám, Light -> Vàng Neon
    val thumbColorAnim by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFFDDDDDD) else GenZTheme.colors.neonYellow,
        label = "thumbColor"
    )

    // Lấy màu border ra ngoài để tránh lỗi @Composable trong Canvas
    val borderColor = GenZTheme.colors.border

    val starAlphaAnim by animateFloatAsState(if (isDarkTheme) 1f else 0f, label = "star")

    Canvas(
        modifier = modifier
            .size(width, height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        val cornerRadius = size.height / 2

        // 1. Vẽ Track Background
        drawRoundRect(
            color = trackColorAnim,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Fill
        )

        drawRoundRect(
            color = borderColor, 
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // 3. Vẽ Sao (Dark Mode)
        if (starAlphaAnim > 0f) {
            val starColor = Color.White.copy(alpha = starAlphaAnim)
            drawSparkle(center = Offset(size.width * 0.25f, size.height * 0.35f), size = 3.dp.toPx(), color = starColor)
            drawSparkle(center = Offset(size.width * 0.5f, size.height * 0.7f), size = 2.dp.toPx(), color = starColor)
        }

        // 4. Vẽ Thumb (Mặt trời / Mặt trăng)
        val thumbX = thumbOffsetAnim.toPx() + (size.height / 2)
        val thumbCenter = Offset(thumbX, size.height / 2)
        val thumbRadius = (size.height / 2) - 4.dp.toPx()

        if (isDarkTheme) {
            // Moon
            drawCrescentMoon(
                center = thumbCenter,
                radius = thumbRadius,
                color = thumbColorAnim
            )
        } else {
            // Sun
            drawSun(
                center = thumbCenter,
                coreRadius = thumbRadius * 0.6f,
                rayLength = thumbRadius * 0.35f,
                rayWidth = 2.dp.toPx(),
                rayGap = 2.dp.toPx(), // Tia nắng sát hơn chút
                color = thumbColorAnim,
                rotationDegrees = 0f
            )
        }

        // 5. Viền cho Thumb (Để nút nổi bật hơn)
        // Lưu ý: Mặt trăng phức tạp nên ta chỉ viền hình tròn bao quanh logic thôi
        // Hoặc đơn giản là không cần viền thumb nếu màu đã đủ tương phản
    }
}

// --- GIỮ NGUYÊN CÁC HÀM VẼ HÌNH HỌC (HELPER FUNCTIONS) ---
// (Đã optimize lại một chút cho gọn)

fun DrawScope.drawCrescentMoon(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        // Hình tròn chính
        addOval(Rect(center, radius))
        // Hình tròn cắt (Lệch phải)
        val cutPath = Path().apply {
            addOval(Rect(center = Offset(center.x + radius * 0.35f, center.y - radius * 0.1f), radius = radius))
        }
        op(this, cutPath, PathOperation.Difference)
    }
    drawPath(path, color)
}

fun DrawScope.drawSun(
    center: Offset, coreRadius: Float, rayLength: Float, rayWidth: Float, rayGap: Float,
    color: Color, numRays: Int = 8, rotationDegrees: Float
) {
    rotate(rotationDegrees, center) {
        drawCircle(color, coreRadius, center)
        val step = 360f / numRays
        repeat(numRays) { i ->
            rotate(i * step, center) {
                drawLine(
                    color,
                    start = Offset(center.x, center.y - (coreRadius + rayGap)),
                    end = Offset(center.x, center.y - (coreRadius + rayGap + rayLength)),
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticBezierTo(center.x, center.y, center.x + size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + size)
        quadraticBezierTo(center.x, center.y, center.x - size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path, color)
}