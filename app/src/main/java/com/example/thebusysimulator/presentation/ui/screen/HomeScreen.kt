package com.example.thebusysimulator.presentation.ui.screen

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.example.thebusysimulator.R
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.example.thebusysimulator.presentation.navigation.Screen
import kotlin.math.PI
import kotlin.math.sin

// AppColors removed - using MaterialTheme.colorScheme instead

const val DEFAULT_PADDING = 44

data class ButtonData(val text: String, val icon: ImageVector)

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
    val colorScheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.background, colorScheme.surface)
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nội dung màn hình Home với các tính năng
        HomeContent(
            navController = navController,
            colorScheme = colorScheme,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
        )

        val navigationButtons = listOf(
            ButtonData("Trang chủ", Icons.Rounded.Home),
            ButtonData("Hồ sơ", Icons.Rounded.Person)
        )
        
        AnimatedNavigationBar(
            buttons = navigationButtons,
            barColor = colorScheme.surface,
            circleColor = colorScheme.primary,
            selectedColor = colorScheme.onPrimary,
            unselectedColor = colorScheme.onSurfaceVariant,
            navController = navController,
            modifier = Modifier.navigationBarsPadding()
        )

        // Hiệu ứng lan tỏa khi click
//        Circle(
//            color = colorScheme.primary.copy(alpha = 0.5f),
//            animationProgress = 0.5f
//        )

//        // Layer chứa các nút có hiệu ứng dính (Gooey)
//        FabGroup(
//            navController = navController,
//            renderEffect = renderEffect,
//            animationProgress = fabAnimationProgress,
//            modifier = Modifier.navigationBarsPadding()
//        )
//
//        // Layer chứa nút chính (không bị dính) để nhận sự kiện click
//        FabGroup(
//            navController = navController,
//            renderEffect = null,
//            animationProgress = fabAnimationProgress,
//            toggleAnimation = toggleAnimation,
//            modifier = Modifier.navigationBarsPadding()
//        )

        Circle(
            color = Color.White,
            animationProgress = clickAnimationProgress
        )
    }
}

@Composable
fun HomeContent(
    navController: NavController,
    colorScheme: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        Text(
            text = "The Busy Simulator",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Chọn tính năng bạn muốn sử dụng",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Feature Cards Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 1: Fake Call và Message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "Cuộc gọi giả",
                    description = "Tạo cuộc gọi giả để giả vờ bận",
                    iconId = R.drawable.ic_call,
                    backgroundColor = colorScheme.secondary,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.FakeCall.route) }
                )
                
                FeatureCard(
                    title = "Tin nhắn",
                    description = "Quản lý và tạo tin nhắn giả",
                    iconId = R.drawable.ic_message,
                    backgroundColor = colorScheme.secondary,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Message.route) }
                )
            }

            // Row 2: Settings và Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    title = "Cài đặt",
                    description = "Tùy chỉnh ứng dụng",
                    iconId = R.drawable.ic_settings,
                    backgroundColor = colorScheme.primaryContainer,
                    iconColor = colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
                
                FeatureCardWithIcon(
                    title = "Hồ sơ",
                    description = "Xem thông tin cá nhân",
                    iconVector = Icons.Rounded.Person,
                    backgroundColor = colorScheme.tertiaryContainer,
                    iconColor = colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    @DrawableRes iconId: Int,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = iconColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun FeatureCardWithIcon(
    title: String,
    description: String,
    iconVector: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = iconColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun Circle(color: Color, animationProgress: Float) {
    val animationValue = sin(PI * animationProgress).toFloat()

    Box(
        modifier = Modifier
            .padding(DEFAULT_PADDING.dp)
            .size(45.dp)
            .scale(2 - animationValue)
            .border(
                width = 2.dp,
                color = color.copy(alpha = color.alpha * animationValue),
                shape = CircleShape
            )
    )
}

@Composable
fun AnimatedNavigationBar(
    buttons: List<ButtonData>,
    barColor: Color,
    circleColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val circleRadius = 26.dp

    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var barSize by remember { mutableStateOf(IntSize(0, 0)) }
    // first item's center offset for Arrangement.SpaceAround
    val offsetStep = remember(barSize) {
        if (barSize.width > 0 && buttons.isNotEmpty()) {
            barSize.width.toFloat() / (buttons.size * 2)
        } else {
            0f
        }
    }
    val offset = remember(selectedItem, offsetStep) {
        if (offsetStep > 0) {
            offsetStep + selectedItem * 2 * offsetStep
        } else {
            0f
        }
    }
    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val targetCircleOffset = remember(offset, circleRadiusPx) {
        IntOffset(offset.toInt() - circleRadiusPx, -circleRadiusPx)
    }
    val offsetTransition = updateTransition(offset, label = "offset transition")
    val circleOffsetTransition = updateTransition(targetCircleOffset, label = "circle offset transition")
    val animation = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)
    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = {
            if (this.initialState == 0f) {
                snap()
            } else {
                animation
            }
        },
        label = "cutout offset"
    ) { it }
    val circleOffset by circleOffsetTransition.animateIntOffset(
        transitionSpec = {
            if (this.initialState == IntOffset(0, 0)) {
                snap<IntOffset>()
            } else {
                spring<IntOffset>(animation.dampingRatio, animation.stiffness)
            }
        },
        label = "circle offset"
    ) { it }
    val barShape = remember(cutoutOffset) {
        BarShape(
            offset = cutoutOffset,
            circleRadius = circleRadius,
            cornerRadius = 25.dp,
        )
    }

    Box(modifier = modifier) {
        Circle(
            modifier = Modifier
                .offset { circleOffset }
                // the circle should be above the bar for accessibility reasons
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            button = buttons[selectedItem],
            iconColor = selectedColor,
        )
        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .graphicsLayer {
                    shape = barShape
                    clip = true
                }
                .fillMaxWidth()
                .background(barColor)
                .height(70.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            buttons.forEachIndexed { index, button ->
                val isSelected = index == selectedItem
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { 
                        selectedItem = index
                        when (index) {
                            0 -> navController.navigate(Screen.Home.route)
                            1 -> navController.navigate(Screen.Profile.route)
                        }
                    },
                    icon = {
                        val iconAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0f else 1f,
                            label = "Navbar item icon"
                        )
                        Icon(
                            imageVector = button.icon,
                            contentDescription = button.text,
                            modifier = Modifier.alpha(iconAlpha)
                        )
                    },
                    label = { Text(button.text) },
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        selectedIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}

private class BarShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp = 5.dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterX = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }
        val cornerDiameter = cornerRadiusPx * 2
        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.5f
            val cutoutLeftX = cutoutCenterX - cutoutEdgeOffset
            val cutoutRightX = cutoutCenterX + cutoutEdgeOffset

            // bottom left
            moveTo(x = 0F, y = size.height)
            // top left
            if (cutoutLeftX > 0) {
                val realLeftCornerDiameter = if (cutoutLeftX >= cornerRadiusPx) {
                    // there is a space between rounded corner and cutout
                    cornerDiameter
                } else {
                    // rounded corner and cutout overlap
                    cutoutLeftX * 2
                }
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = realLeftCornerDiameter,
                        bottom = realLeftCornerDiameter
                    ),
                    startAngleDegrees = 180.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }
            lineTo(cutoutLeftX, 0f)
            // cutout
            cubicTo(
                x1 = cutoutCenterX - cutoutRadius,
                y1 = 0f,
                x2 = cutoutCenterX - cutoutRadius,
                y2 = cutoutRadius,
                x3 = cutoutCenterX,
                y3 = cutoutRadius,
            )
            cubicTo(
                x1 = cutoutCenterX + cutoutRadius,
                y1 = cutoutRadius,
                x2 = cutoutCenterX + cutoutRadius,
                y2 = 0f,
                x3 = cutoutRightX,
                y3 = 0f,
            )
            // top right
            if (cutoutRightX < size.width) {
                val realRightCornerDiameter = if (cutoutRightX <= size.width - cornerRadiusPx) {
                    cornerDiameter
                } else {
                    (size.width - cutoutRightX) * 2
                }
                arcTo(
                    rect = Rect(
                        left = size.width - realRightCornerDiameter,
                        top = 0f,
                        right = size.width,
                        bottom = realRightCornerDiameter
                    ),
                    startAngleDegrees = -90.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }
            // bottom right
            lineTo(x = size.width, y = size.height)
            close()
        }
    }
}

@Composable
private fun Circle(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    radius: Dp,
    button: ButtonData,
    iconColor: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .clip(CircleShape)
            .background(color),
    ) {
        AnimatedContent(
            targetState = button.icon, label = "Bottom bar circle icon",
        ) { targetIcon ->
            Icon(targetIcon, button.text, tint = iconColor)
        }
    }
}

//@Composable
//fun FabGroup(
//    navController: NavController,
//    animationProgress: Float = 0f,
//    renderEffect: androidx.compose.ui.graphics.RenderEffect? = null,
//    toggleAnimation: () -> Unit = { },
//    modifier: Modifier = Modifier
//) {
//    val colorScheme = MaterialTheme.colorScheme
//    Box(
//        modifier
//            .fillMaxSize()
//            .graphicsLayer { this.renderEffect = renderEffect }
//            .padding(bottom = DEFAULT_PADDING.dp),
//        contentAlignment = Alignment.BottomCenter
//    ) {
//        // Nút con 1: Call (Màu phụ)
//        AnimatedFab(
//            iconId = R.drawable.ic_call,
//            modifier = Modifier
//                .padding(
//                    PaddingValues(
//                        bottom = 72.dp,
//                        end = 210.dp
//                    ) * FastOutSlowInEasing.transform(0f, 0.8f, animationProgress)
//                ),
//            opacity = LinearEasing.transform(0.2f, 0.7f, animationProgress),
//            backgroundColor = colorScheme.secondary,
//            onClick = { navController.navigate(Screen.FakeCall.route) }
//        )
//
//        // Nút con 2: Settings (Màu trắng, icon màu chính)
//        AnimatedFab(
//            iconId = R.drawable.ic_settings,
//            modifier = Modifier.padding(
//                PaddingValues(
//                    bottom = 88.dp,
//                ) * FastOutSlowInEasing.transform(0.1f, 0.9f, animationProgress)
//            ),
//            opacity = LinearEasing.transform(0.3f, 0.8f, animationProgress),
//            backgroundColor = colorScheme.onBackground,
//            iconColor = colorScheme.primary,
//            onClick = { navController.navigate(Screen.Settings.route) }
//        )
//
//        // Nút con 3: ShoppingCart (Màu phụ)
//        AnimatedFab(
//            iconId = R.drawable.ic_message,
//            modifier = Modifier.padding(
//                PaddingValues(
//                    bottom = 72.dp,
//                    start = 210.dp
//                ) * FastOutSlowInEasing.transform(0.2f, 1.0f, animationProgress)
//            ),
//            opacity = LinearEasing.transform(0.4f, 0.9f, animationProgress),
//            backgroundColor = colorScheme.secondary,
//            onClick = { navController.navigate(Screen.Message.route) }
//        )
//
//        // Nút nền (bị nhỏ lại khi mở)
//        AnimatedFab(
//            modifier = Modifier
//                .scale(1f - LinearEasing.transform(0.5f, 0.85f, animationProgress)),
//            backgroundColor = colorScheme.primary
//        )
//
//        // Nút chính (Dấu + xoay)
//        AnimatedFab(
//            iconId = R.drawable.ic_add,
//            modifier = Modifier
//                .rotate(
//                    225 * FastOutSlowInEasing
//                        .transform(0.35f, 0.65f, animationProgress)
//                ),
//            onClick = toggleAnimation,
//            backgroundColor = Color.Transparent,
//            iconColor = Color.White
//        )
//    }
//}
//
//@Composable
//fun AnimatedFab(
//    modifier: Modifier = Modifier,
//    @DrawableRes iconId: Int? = null,
//    opacity: Float = 1f,
//    backgroundColor: Color = MaterialTheme.colorScheme.primary,
//    iconColor: Color = Color.White,
//    onClick: () -> Unit = {}
//) {
//    Box(
//        modifier = modifier
//            .scale(1.2f)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,
//                onClick = onClick
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        // Background Circle
//        Box(
//            modifier = Modifier
//                .size(45.dp)
//                .clip(CircleShape)
//                .background(backgroundColor)
//        )
//
//        // Icon
//        iconId?.let {
//            Icon(
//                painter = painterResource(id = it),
//                contentDescription = null,
//                tint = iconColor.copy(alpha = opacity),
//            )
//        }
//    }
//}