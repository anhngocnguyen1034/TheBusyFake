package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thebusysimulator.R
import kotlinx.coroutines.delay

/**
 * "Sticker Pop" splash — một card sticker (nền trắng, viền đen dày, bóng cứng lệch)
 * bật lên giữa nền vàng brand, đúng ngôn ngữ Neo-Brutalism của app. Sau ~1.7s tự
 * gọi [onFinish] để điều hướng vào Home.
 */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    // Màu brand cố định (không theo theme) để splash luôn bold & nhất quán.
    val brandYellow = Color(0xFFF4D738)
    val ink = Color(0xFF000000)
    val paper = Color(0xFFFFFFFF)
    val cardShape = RoundedCornerShape(24.dp)

    var started by remember { mutableStateOf(false) }

    // Card "pop" vào bằng spring nảy nhẹ + chỉnh nghiêng như miếng sticker dán.
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (started) -4f else 12f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )
    // Bóng cứng trượt ra sau khi card đã đáp xuống → cảm giác nổi 3D.
    val shadowOffset by animateDpAsState(
        targetValue = if (started) 9.dp else 0.dp,
        animationSpec = tween(durationMillis = 450, delayMillis = 160),
        label = "shadow"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 420),
        label = "taglineAlpha"
    )
    val taglineOffset by animateDpAsState(
        targetValue = if (started) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 400, delayMillis = 420),
        label = "taglineOffset"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1700)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandYellow),
        contentAlignment = Alignment.Center
    ) {
        // Họa tiết chấm bi nền (giống Home) — đen mờ.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 22.dp.toPx()
            val dot = ink.copy(alpha = 0.06f)
            var x = 0f
            while (x <= size.width) {
                var y = 0f
                while (y <= size.height) {
                    drawCircle(color = dot, radius = 1.5.dp.toPx(), center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // --- Sticker card ---
            Box(
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                // Lớp bóng cứng (offset xuống phải, không blur)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = shadowOffset, y = shadowOffset)
                        .background(ink, cardShape)
                )
                // Mặt sticker
                Column(
                    modifier = Modifier
                        .background(paper, cardShape)
                        .border(width = 4.dp, color = ink, shape = cardShape)
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "📞", fontSize = 34.sp)
                        Text(text = "💬", fontSize = 34.sp)
                        Text(text = "🔔", fontSize = 34.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.busy_simulator),
                        color = ink,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Tagline ---
            Text(
                text = stringResource(R.string.fake_it_till_you_make_it),
                color = ink,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .offset(y = taglineOffset)
            )
        }
    }
}
