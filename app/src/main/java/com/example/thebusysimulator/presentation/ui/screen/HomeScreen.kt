package com.example.thebusysimulator.presentation.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.thebusysimulator.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavOptions
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.navigationBarPadding
import com.example.thebusysimulator.presentation.ui.statusBarPadding

@Composable
fun MainScreen(navController: NavController) {
    MainContainer(navController = navController) {
        MainScreenUI(navController = navController)
    }
}

@Composable
fun MainContainer(
    navController: NavController, content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.background, colorScheme.surface)
                )
            )
    ) {
        // Main content
        content()

        // Bottom navigation - only show on Home and Settings routes
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: ""
        val showBottomNav =
            currentRoute == Screen.Home.route || currentRoute == Screen.Settings.route

        if (showBottomNav) {
            CustomBottomNavigation(
                navController = navController,
                colorScheme = colorScheme,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarPadding()
            )
        }
    }
}

@Composable
fun MainScreenUI(
    navController: NavController
) {
    val colorScheme = MaterialTheme.colorScheme
    // Main content with feature cards
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title
        Text(
            text = "The Busy Simulator",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FeatureCard(
            title = "Fake Call",
            description = "Tạo cuộc gọi giả với số điện thoại và tên người gọi tùy chỉnh",
            iconId = R.drawable.ic_call,
            onClick = {
                navController.navigate(
                    route = Screen.FakeCall.route,
                    navOptions = NavOptions.Builder()
                        .setPopUpTo(Screen.Home.route, inclusive = false).setLaunchSingleTop(true)
                        .build()
                )
            },
            gradientColors = listOf(
                colorScheme.primary, colorScheme.secondary
            )
        )

        FeatureCard(
            title = "Fake Chat Message",
            description = "Tạo tin nhắn chat giả với nội dung và thời gian tùy chỉnh",
            iconId = R.drawable.ic_message,
            onClick = {
                navController.navigate(
                    route = Screen.Message.route,
                    navOptions = NavOptions.Builder()
                        .setPopUpTo(Screen.Home.route, inclusive = false).setLaunchSingleTop(true)
                        .build()
                )
            },
            gradientColors = listOf(
                colorScheme.secondary, colorScheme.tertiary
            )
        )

        FeatureCard(
            title = "Fake Notification",
            description = "Tạo thông báo tin nhắn giả hiển thị trên màn hình khóa",
            iconId = R.drawable.ic_message,
            onClick = {
                navController.navigate(
                    route = Screen.FakeMessage.route,
                    navOptions = NavOptions.Builder()
                        .setPopUpTo(Screen.Home.route, inclusive = false).setLaunchSingleTop(true)
                        .build()
                )
            },
            gradientColors = listOf(
                colorScheme.tertiary, colorScheme.primary
            )
        )
    }
}
@Composable
fun CustomBottomNavigation(
    navController: NavController,
    colorScheme: androidx.compose.material3.ColorScheme = MaterialTheme.colorScheme,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHomeSelected = currentRoute == Screen.Home.route

    // Dùng BoxWithConstraints để lấy chiều rộng màn hình, từ đó tính vị trí tâm icon
    BoxWithConstraints(
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val width = constraints.maxWidth.toFloat()

        // Tính toán vị trí tâm của đường cong (Curve)
        // Vì ta chia 2 nút đều nhau bằng weight(1f), nên tâm nút trái là 25% width, nút phải là 75% width
        val homeCenter = width * 0.25f
        val settingsCenter = width * 0.75f

        // Animation dịch chuyển vị trí đường cong
        val animatedCenter by animateFloatAsState(
            targetValue = if (isHomeSelected) homeCenter else settingsCenter,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "curveAnimation"
        )

        // --- 1. LỚP CANVAS (VẼ THANH BAR CÓ ĐƯỜNG LÕM DI CHUYỂN) ---
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp) // Chiều cao của background
                .align(Alignment.BottomCenter)
        ) {
            val curveDepth = 40.dp.toPx() // Độ sâu của lõm (phải sâu để chứa nút tròn)
            val curveWidth = 90.dp.toPx() // Độ rộng miệng lõm
            val cornerRadius = 25.dp.toPx()

            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, cornerRadius)
                quadraticBezierTo(0f, 0f, cornerRadius, 0f)

                // Vẽ đường cong lõm tại vị trí animatedCenter
                val curveStart = animatedCenter - (curveWidth / 2)
                val curveEnd = animatedCenter + (curveWidth / 2)

                lineTo(curveStart, 0f)

                // Vẽ lõm xuống (hình cái bát)
                cubicTo(
                    curveStart + (curveWidth * 0.25f), 0f,
                    animatedCenter - (curveWidth * 0.15f), curveDepth,
                    animatedCenter, curveDepth
                )
                cubicTo(
                    animatedCenter + (curveWidth * 0.15f), curveDepth,
                    curveEnd - (curveWidth * 0.25f), 0f,
                    curveEnd, 0f
                )

                lineTo(size.width - cornerRadius, 0f)
                quadraticBezierTo(size.width, 0f, size.width, cornerRadius)
                lineTo(size.width, size.height)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface.copy(alpha = 0.98f),
                        colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    )
                )
            )

            // Vẽ viền mỏng
            drawPath(
                path = path,
                color = colorScheme.outline.copy(alpha = 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        // --- 2. CÁC NÚT BẤM (ICON) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(80.dp), // Khớp với chiều cao Canvas
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Home (chiếm 50% diện tích để canh giữa chuẩn)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingNavItem(
                    isSelected = isHomeSelected,
                    icon = Icons.Rounded.Home,
                    title = "Home",
                    colorScheme = colorScheme,
                    onClick = {
                        if (!isHomeSelected) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // Nút Settings (chiếm 50% diện tích)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingNavItem(
                    isSelected = !isHomeSelected, // Là settings
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    colorScheme = colorScheme,
                    onClick = {
                        val isSettings = currentRoute == Screen.Settings.route
                        if (!isSettings) {
                            try {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                    // Thêm restoreState để tránh màn hình trắng
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // Fallback nếu có lỗi navigation
                                navController.navigate(Screen.Settings.route)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colorScheme: androidx.compose.material3.ColorScheme,
    onClick: () -> Unit
) {
    // Animation cho việc nổi lên (offset Y)
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-30).dp else 0.dp, // Nếu chọn thì bay lên 30dp
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy) // Hiệu ứng nảy
    )

    // Animation cho kích thước nút tròn background
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 48.dp,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .size(animatedSize)
            .clip(CircleShape)
            .background(
                if (isSelected) colorScheme.primary else Color.Transparent
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            // Nếu chọn -> màu trắng (nổi trên nền Primary), Không chọn -> màu xám
            tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    @DrawableRes iconId: Int,
    onClick: () -> Unit,
    gradientColors: List<Color>
) {
    val colorScheme = MaterialTheme.colorScheme
    // Kiểm tra theme dựa trên độ sáng của background
    val isDarkTheme = colorScheme.background.red < 0.5f && 
                      colorScheme.background.green < 0.5f && 
                      colorScheme.background.blue < 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                Color.White.copy(alpha = 0.1f)
            } else {
                colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(gradientColors)
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}