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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title
        Text(
            text = "The Busy Simulator",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 2.dp)
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

    BoxWithConstraints(
        modifier = modifier
            .height(80.dp) // Tăng chiều cao tổng thể một chút để nút bay lên không bị cắt
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val width = constraints.maxWidth.toFloat()

        val homeCenter = width * 0.25f
        val settingsCenter = width * 0.75f

        val animatedCenter by animateFloatAsState(
            targetValue = if (isHomeSelected) homeCenter else settingsCenter,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "curveAnimation"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Chiều cao background
                .align(Alignment.BottomCenter)
        ) {
            // --- THÔNG SỐ QUAN TRỌNG ĐỂ TẠO KHOẢNG HỞ ---
            val curveDepth = 20.dp.toPx() // Sâu hơn một chút để chứa nút
            val curveWidth = 90.dp.toPx() // RỘNG HƠN NÚT NHIỀU (khoảng hở 2 bên)
            val cornerRadius = 20.dp.toPx()

            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, cornerRadius)
                quadraticBezierTo(0f, 0f, cornerRadius, 0f)

                val curveStart = animatedCenter - (curveWidth / 2)
                val curveEnd = animatedCenter + (curveWidth / 2)

                lineTo(curveStart, 0f)

                // Vẽ đường cong mềm và rộng (U-Shape)
                cubicTo(
                    curveStart + (curveWidth * 0.2f), 0f,           // Control point 1 (giữ ngang lâu hơn)
                    animatedCenter - (curveWidth * 0.1f), curveDepth, // Control point 2 (lao xuống dốc)
                    animatedCenter, curveDepth                      // Đáy
                )
                cubicTo(
                    animatedCenter + (curveWidth * 0.1f), curveDepth,
                    curveEnd - (curveWidth * 0.2f), 0f,
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

            drawPath(
                path = path,
                color = colorScheme.outline.copy(alpha = 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Button Container
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

            // Settings Button Container
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FloatingNavItem(
                    isSelected = !isHomeSelected,
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    colorScheme = colorScheme,
                    onClick = {
                        val isSettings = currentRoute == Screen.Settings.route
                        if (!isSettings) {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
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
    // --- CHỈNH ĐỘ CAO BAY LÊN ---
    // Bay lên -42dp để tạo cảm giác lơ lửng rõ ràng, không chạm vào phần lõm
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-42).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // --- CHỈNH KÍCH THƯỚC NÚT ---
    // Tăng lên 50dp cho tròn đẹp, dễ bấm (Vẫn nhỏ hơn curveWidth 90dp => Có khoảng hở 2 bên 20dp)
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 50.dp else 40.dp,
        animationSpec = tween(300)
    )

    // Hiệu ứng icon size
    val animatedIconSize by animateDpAsState(
        targetValue = if (isSelected) 26.dp else 24.dp,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .size(animatedSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorScheme.primary else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(animatedIconSize)
            )
        }
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
        shape = RoundedCornerShape(16.dp),
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
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(gradientColors)
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }
        }
    }
}