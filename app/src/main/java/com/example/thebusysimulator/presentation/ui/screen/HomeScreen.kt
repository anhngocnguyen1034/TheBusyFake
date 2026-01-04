package com.example.thebusysimulator.presentation.ui.screen

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thebusysimulator.presentation.navigation.Screen
import kotlin.math.PI
import kotlin.math.sin

object AppColors {
    val BackgroundStart = Color(0xFF1F2029)
    val BackgroundEnd = Color(0xFF2C2D3A)
    val BottomNavBg = Color(0xFFFFFFFF)
    val Accent = Color(0xFF6C63FF)
    val AccentSecondary = Color(0xFFFF6584)
    val IconInactive = Color(0xFFB0B3C7)
}

const val DEFAULT_PADDING = 44

fun Easing.transform(from: Float, to: Float, value: Float): Float {
    if (value < from) return 0f
    if (value > to) return 1f
    val range = to - from
    val rangeValue = (value - from) / range
    return this.transform(rangeValue)
}

operator fun PaddingValues.times(value: Float): PaddingValues {
    val layoutDirection = LayoutDirection.Ltr
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection) * value,
        top = this.calculateTopPadding() * value,
        end = this.calculateEndPadding(layoutDirection) * value,
        bottom = this.calculateBottomPadding() * value
    )
}

@RequiresApi(Build.VERSION_CODES.S)
private fun getRenderEffect(): RenderEffect {
    // Giữ nguyên logic Blur + Alpha Threshold để tạo hiệu ứng Gooey
    val blurEffect = RenderEffect
        .createBlurEffect(80f, 80f, Shader.TileMode.MIRROR)

    val alphaMatrix = RenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 50f, -5000f
                )
            )
        )
    )

    return RenderEffect
        .createChainEffect(alphaMatrix, blurEffect)
}

@Composable
fun MainScreen(navController: NavController) {
    val isMenuExtended = remember { mutableStateOf(false) }

    // Logic Animation giữ nguyên
    val fabAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ), label = "fabAnimation"
    )

    val clickAnimationProgress by animateFloatAsState(
        targetValue = if (isMenuExtended.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearEasing
        ), label = "clickAnimation"
    )

    val renderEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getRenderEffect().asComposeRenderEffect()
    } else {
        null
    }

    MainScreenUI(
        navController = navController,
        renderEffect = renderEffect,
        fabAnimationProgress = fabAnimationProgress,
        clickAnimationProgress = clickAnimationProgress
    ) {
        isMenuExtended.value = !isMenuExtended.value
    }
}

@Composable
fun MainScreenUI(
    navController: NavController,
    renderEffect: androidx.compose.ui.graphics.RenderEffect?,
    fabAnimationProgress: Float = 0f,
    clickAnimationProgress: Float = 0f,
    toggleAnimation: () -> Unit = { }
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.BackgroundStart, AppColors.BackgroundEnd)
                )
            ) ,
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nội dung màn hình giả lập (Placeholder)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Bạn có thể đặt nội dung game/app ở đây
        }

        CustomBottomNavigation(navController = navController)

        // Hiệu ứng lan tỏa khi click
        Circle(
            color = AppColors.Accent.copy(alpha = 0.5f),
            animationProgress = 0.5f
        )

        // Layer chứa các nút có hiệu ứng dính (Gooey)
        FabGroup(
            navController = navController,
            renderEffect = renderEffect,
            animationProgress = fabAnimationProgress
        )

        // Layer chứa nút chính (không bị dính) để nhận sự kiện click
        FabGroup(
            navController = navController,
            renderEffect = null,
            animationProgress = fabAnimationProgress,
            toggleAnimation = toggleAnimation
        )

        Circle(
            color = Color.White,
            animationProgress = clickAnimationProgress
        )
    }
}

@Composable
fun Circle(color: Color, animationProgress: Float) {
    val animationValue = sin(PI * animationProgress).toFloat()

    Box(
        modifier = Modifier
            .padding(DEFAULT_PADDING.dp)
            .size(56.dp)
            .scale(2 - animationValue)
            .border(
                width = 2.dp,
                color = color.copy(alpha = color.alpha * animationValue),
                shape = CircleShape
            )
    )
}

@Composable
fun CustomBottomNavigation(navController: NavController) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, 20f) // Bo nhẹ góc trên cùng
                quadraticBezierTo(0f, 0f, 20f, 0f)

                lineTo(size.width * 0.35f, 0f)

                // Đường cong mượt hơn (Smoother Cubic Bezier)
                cubicTo(
                    size.width * 0.45f, 0f,
                    size.width * 0.40f, size.height * 0.65f,
                    size.width * 0.5f, size.height * 0.65f
                )
                cubicTo(
                    size.width * 0.60f, size.height * 0.65f,
                    size.width * 0.55f, 0f,
                    size.width * 0.65f, 0f
                )

                lineTo(size.width - 20f, 0f)
                quadraticBezierTo(size.width, 0f, size.width, 20f)
                lineTo(size.width, size.height)
                close()
            }
            // Vẽ bóng đổ nhẹ cho thanh nav (giả lập elevation)
            drawPath(path, color = AppColors.BottomNavBg)
        }

        // Icon Buttons trên thanh Navigation
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .align(Alignment.Center)
                .offset(y = (-10).dp)
        ) {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = AppColors.IconInactive,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = AppColors.IconInactive,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun FabGroup(
    navController: NavController,
    animationProgress: Float = 0f,
    renderEffect: androidx.compose.ui.graphics.RenderEffect? = null,
    toggleAnimation: () -> Unit = { }
) {
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { this.renderEffect = renderEffect }
            .padding(bottom = DEFAULT_PADDING.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nút con 1: Call (Màu phụ)
        AnimatedFab(
            icon = Icons.Rounded.Call,
            modifier = Modifier
                .padding(
                    PaddingValues(
                        bottom = 72.dp,
                        end = 210.dp
                    ) * FastOutSlowInEasing.transform(0f, 0.8f, animationProgress)
                ),
            opacity = LinearEasing.transform(0.2f, 0.7f, animationProgress),
            backgroundColor = AppColors.AccentSecondary,
            onClick = { navController.navigate(Screen.FakeCall.route) }
        )

        // Nút con 2: Settings (Màu trắng, icon màu chính)
        AnimatedFab(
            icon = Icons.Rounded.Settings,
            modifier = Modifier.padding(
                PaddingValues(
                    bottom = 88.dp,
                ) * FastOutSlowInEasing.transform(0.1f, 0.9f, animationProgress)
            ),
            opacity = LinearEasing.transform(0.3f, 0.8f, animationProgress),
            backgroundColor = Color.White,
            iconColor = AppColors.Accent,
            onClick = { navController.navigate(Screen.Settings.route) }
        )

        // Nút con 3: ShoppingCart (Màu phụ)
        AnimatedFab(
            icon = Icons.Rounded.ShoppingCart,
            modifier = Modifier.padding(
                PaddingValues(
                    bottom = 72.dp,
                    start = 210.dp
                ) * FastOutSlowInEasing.transform(0.2f, 1.0f, animationProgress)
            ),
            opacity = LinearEasing.transform(0.4f, 0.9f, animationProgress),
            backgroundColor = AppColors.AccentSecondary,
            onClick = { navController.navigate(Screen.Message.route) }
        )

        // Nút nền (bị nhỏ lại khi mở)
        AnimatedFab(
            modifier = Modifier
                .scale(1f - LinearEasing.transform(0.5f, 0.85f, animationProgress)),
            backgroundColor = AppColors.Accent
        )

        // Nút chính (Dấu + xoay)
        AnimatedFab(
            icon = Icons.Rounded.Add,
            modifier = Modifier
                .rotate(
                    225 * FastOutSlowInEasing
                        .transform(0.35f, 0.65f, animationProgress)
                ),
            onClick = toggleAnimation,
            backgroundColor = Color.Transparent,
            iconColor = Color.White
        )
    }
}

@Composable
fun AnimatedFab(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    opacity: Float = 1f,
    backgroundColor: Color = AppColors.Accent,
    iconColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .scale(1.2f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        )

        // Icon
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = iconColor.copy(alpha = opacity),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
