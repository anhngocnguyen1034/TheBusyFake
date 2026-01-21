package com.example.thebusysimulator.presentation.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.navigationBarPadding
import com.example.thebusysimulator.presentation.ui.statusBarPadding

val GenZYellow = Color(0xFFF4D738)
val GenZPink = Color(0xFFFF90E8)
val GenZBlue = Color(0xFF51E5FF)
val GenZPurple = Color(0xFF9D65FF)
val GenZGreen = Color(0xFF00E054)

// --- LIGHT MODE COLORS ---
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFF000000)
val LightText = Color(0xFF000000)

// --- DARK MODE COLORS ---
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkBorder = Color(0xFFFFFFFF)
val DarkText = Color(0xFFFFFFFF)

// --- THEME DATA CLASS ---
data class GenZThemeColors(
    val background: Color,
    val surface: Color,
    val border: Color,
    val text: Color,
    val shadow: Color,
    val pattern: Color
)

// Extension function để tính luminance
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

@Composable
fun MainScreen(navController: NavController) {
    val theme = getGenZTheme()
    MainContainer(navController = navController, theme = theme) {
        MainScreenUI(navController = navController, theme = theme)
    }
}

@Composable
fun MainContainer(
    navController: NavController,
    theme: GenZThemeColors,
    content: @Composable (() -> Unit)
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Họa tiết nền (Background Pattern - Dotted)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 20.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawCircle(
                        color = theme.pattern,
                        radius = 1.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }

        // Main content
        content()

        // Bottom navigation
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: ""
        val showBottomNav =
            currentRoute == Screen.Home.route || currentRoute == Screen.Settings.route

        if (showBottomNav) {
            GenZBottomNavigation(
                navController = navController,
                theme = theme,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarPadding()
            )
        }
    }
}

@Composable
fun MainScreenUI(
    navController: NavController,
    theme: GenZThemeColors
) {
    // Main content with feature cards
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header "Gen Z" Style
        Box(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)) {
            Text(
                text = "BUSY\nSIMULATOR",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = theme.text,
                lineHeight = 40.sp
            )
            // Dấu chấm trang trí
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-5).dp)
                    .size(15.dp)
                    .background(GenZPink, CircleShape)
                    .border(1.dp, theme.border, CircleShape)
            )
        }

        Text(
            text = "Fake it till you make it \uD83D\uDE0E",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = theme.text.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Các Card theo phong cách Neo-Brutalism
        NeoBrutalistCard(
            title = "FAKE CALL",
            description = "Tạo cuộc gọi ảo tung chảo",
            iconId = R.drawable.ic_call,
            accentColor = GenZYellow,
            theme = theme,
            onClick = {
                navController.navigate(Screen.FakeCall.route)
            }
        )

        NeoBrutalistCard(
            title = "FAKE CHAT",
            description = "Nhắn tin 'sống ảo' cực nghệ",
            iconId = R.drawable.ic_message,
            accentColor = GenZPink,
            theme = theme,
            onClick = {
                navController.navigate(Screen.Message.route)
            }
        )

        NeoBrutalistCard(
            title = "NOTIFICATIONS",
            description = "Nổ thông báo liên tục cho ngầu",
            iconId = R.drawable.ic_message,
            accentColor = GenZBlue,
            theme = theme,
            onClick = {
                navController.navigate(Screen.FakeMessage.route)
            }
        )
    }
}

/**
 * GEN Z STYLE CARD: Viền dày + Bóng cứng (Hard Shadow)
 * Hỗ trợ Dark/Light mode
 */
@Composable
fun NeoBrutalistCard(
    title: String,
    description: String,
    @DrawableRes iconId: Int,
    accentColor: Color,
    theme: GenZThemeColors,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation khi nhấn: Thụt xuống (shadow biến mất)
    val offsetState by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(bottom = 4.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Lớp Shadow (Cố định ở dưới)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(theme.shadow, RoundedCornerShape(12.dp))
        )

        // Lớp Content (Di chuyển khi nhấn)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetState, y = offsetState)
                .background(theme.surface, RoundedCornerShape(12.dp))
                .border(2.dp, theme.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon Box vuông vức
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor, RoundedCornerShape(8.dp))
                        .border(2.dp, theme.border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconId),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = theme.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.text.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * GEN Z BOTTOM NAVIGATION: Giữ lại đường cong + hỗ trợ Dark/Light mode
 */
@Composable
fun GenZBottomNavigation(
    navController: NavController,
    theme: GenZThemeColors,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeSelected = currentRoute == Screen.Home.route

    BoxWithConstraints(
        modifier = modifier
            .height(84.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val width = constraints.maxWidth.toFloat()
        val homeCenter = width * 0.25f
        val settingsCenter = width * 0.75f

        val animatedCenter by animateFloatAsState(
            targetValue = if (isHomeSelected) homeCenter else settingsCenter,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            label = "curveAnimation"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
        ) {
            val curveDepth = 24.dp.toPx()
            val curveWidth = 90.dp.toPx()

            val path = Path().apply {
                moveTo(0f, 0f)

                val curveStart = animatedCenter - (curveWidth / 2)
                val curveEnd = animatedCenter + (curveWidth / 2)
                lineTo(curveStart, 0f)

                cubicTo(
                    curveStart + (curveWidth * 0.1f), 0f,
                    animatedCenter - (curveWidth * 0.15f), curveDepth,
                    animatedCenter, curveDepth
                )
                cubicTo(
                    animatedCenter + (curveWidth * 0.15f), curveDepth,
                    curveEnd - (curveWidth * 0.1f), 0f,
                    curveEnd, 0f
                )

                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Fill nền theo theme
            drawPath(
                path = path,
                color = theme.surface
            )

            // Vẽ viền theo theme
            drawPath(
                path = path,
                color = theme.border,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                GenZNavItem(
                    isSelected = isHomeSelected,
                    icon = Icons.Rounded.Home,
                    accentColor = GenZYellow,
                    theme = theme,
                    onClick = {
                        if (!isHomeSelected) navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            // Settings
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                GenZNavItem(
                    isSelected = !isHomeSelected,
                    icon = Icons.Rounded.Settings,
                    accentColor = GenZPurple,
                    theme = theme,
                    onClick = {
                        if (isHomeSelected) navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun GenZNavItem(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    theme: GenZThemeColors,
    onClick: () -> Unit
) {
    // Animation bay và xoay nhẹ
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-45).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isSelected) -10f else 0f
    )

    Box(
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .rotate(animatedRotation)
            .size(52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .background(
                color = if (isSelected) accentColor else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = theme.border,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.Black else theme.text.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
    }
}