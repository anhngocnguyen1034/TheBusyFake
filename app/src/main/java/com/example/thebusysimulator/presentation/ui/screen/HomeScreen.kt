package com.example.thebusysimulator.presentation.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.thebusysimulator.R
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.MaterialTheme
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
    MainScreenUI(navController = navController)
}

@Composable
fun MainScreenUI(
    navController: NavController
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
                            .setPopUpTo(Screen.Home.route, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                },
                gradientColors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
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
                            .setPopUpTo(Screen.Home.route, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                },
                gradientColors = listOf(
                    colorScheme.secondary,
                    colorScheme.tertiary
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
                            .setPopUpTo(Screen.Home.route, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                },
                gradientColors = listOf(
                    colorScheme.tertiary,
                    colorScheme.primary
                )
            )
        }

        CustomBottomNavigation(
            navController = navController,
            colorScheme = colorScheme,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarPadding()
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
    val isSettingsSelected = currentRoute == Screen.Settings.route
    
    Box(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Curved bottom navigation background với Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val curveHeight = 25.dp.toPx()
            val curveWidth = 80.dp.toPx()
            val cornerRadius = 25.dp.toPx()
            val centerX = size.width / 2f
            
            val path = Path().apply {
                // Bắt đầu từ góc dưới bên trái
                moveTo(0f, size.height)
                
                // Đường thẳng lên đến điểm bắt đầu của curve bên trái
                lineTo(0f, cornerRadius)
                
                // Góc tròn bên trái
                quadraticBezierTo(0f, 0f, cornerRadius, 0f)
                
                // Đường thẳng đến điểm bắt đầu của curve giữa (bên trái)
                val leftCurveStart = centerX - curveWidth / 2f - 15.dp.toPx()
                lineTo(leftCurveStart, 0f)
                
                // Curve cong lên ở giữa (bên trái) - tạo đường cong mượt mà
                cubicTo(
                    leftCurveStart + 15.dp.toPx(), 0f,
                    centerX - curveWidth / 3f, -curveHeight * 0.7f,
                    centerX - curveWidth / 6f, -curveHeight
                )
                
                // Curve cong lên ở giữa (bên phải) - tạo đường cong đối xứng
                cubicTo(
                    centerX + curveWidth / 6f, -curveHeight,
                    centerX + curveWidth / 3f, -curveHeight * 0.7f,
                    centerX + curveWidth / 2f + 15.dp.toPx(), 0f
                )
                
                // Đường thẳng đến góc bên phải
                val rightCurveStart = size.width - cornerRadius
                lineTo(rightCurveStart, 0f)
                
                // Góc tròn bên phải
                quadraticBezierTo(size.width, 0f, size.width, cornerRadius)
                
                // Đường thẳng xuống góc dưới bên phải
                lineTo(size.width, size.height)
                
                // Đóng path
                close()
            }
            
            // Vẽ background với gradient
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.surface.copy(alpha = 0.95f),
                        colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    )
                )
            )
            
            // Vẽ border mỏng ở trên để tạo độ sâu
            drawPath(
                path = path,
                color = colorScheme.outline.copy(alpha = 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        // Icon Buttons trên thanh Navigation
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
                .align(Alignment.Center)
                .offset(y = (-8).dp)
        ) {
            // Home Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { 
                        navController.navigate(
                            route = Screen.Home.route,
                            navOptions = NavOptions.Builder()
                                .setPopUpTo(Screen.Home.route, inclusive = true)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Home",
                        tint = if (isHomeSelected) {
                            colorScheme.primary
                        } else {
                            colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Indicator dot
                if (isHomeSelected) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Settings Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { 
                        navController.navigate(
                            route = Screen.Settings.route,
                            navOptions = NavOptions.Builder()
                                .setPopUpTo(Screen.Home.route, inclusive = false)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = if (isSettingsSelected) {
                            colorScheme.primary
                        } else {
                            colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Indicator dot
                if (isSettingsSelected) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary)
                    )
                }
            }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    ),
                contentAlignment = Alignment.Center
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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