package com.example.thebusysimulator.presentation.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.ui.theme.GenZBlue
import com.example.thebusysimulator.presentation.ui.theme.GenZPink
import com.example.thebusysimulator.presentation.ui.theme.GenZYellow
import kotlinx.coroutines.launch

/** Một slide onboarding: ảnh mockup điện thoại + tiêu đề, nút được vẽ bằng Compose. */
private data class IntroSlide(
    @param:DrawableRes val image: Int,
    @param:StringRes val label: Int,
    val accent: Color
)

private val introSlides = listOf(
    IntroSlide(R.drawable.intro_call, R.string.intro_slide_call, GenZYellow),
    IntroSlide(R.drawable.intro_chat, R.string.intro_slide_chat, GenZPink),
    IntroSlide(R.drawable.intro_notification, R.string.intro_slide_notification, GenZBlue)
)

// Bảng màu Neo-Brutalism cố định cho intro: nền ảnh luôn sáng nên viền/bóng đen
// và mặt nút trắng cho tương phản tốt trên cả 3 màu (vàng / hồng / xanh).
private val IntroBorder = Color.Black
private val IntroShadow = Color.Black
private val IntroButtonSurface = Color.White
private val IntroText = Color.Black

/**
 * Màn giới thiệu (onboarding) 3 slide. Mỗi slide gồm ảnh mockup điện thoại full-bleed
 * và các nút thật (Skip / Next / Get started) được dựng bằng Compose theo phong cách
 * Neo-Brutalism. [onFinish] được gọi khi bấm Skip hoặc Get started ở slide cuối.
 */
@Composable
fun IntroScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { introSlides.size })
    val scope = rememberCoroutineScope()

    val page = pagerState.currentPage
    val slide = introSlides[page]
    val isLast = page == introSlides.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Nền cùng màu ảnh để lấp khoảng trống trên màn hình cao/rộng khác tỉ lệ.
            .background(slide.accent)
    ) {
        // Chỉ ảnh mockup lướt theo pager; box đáy cố định đè lên phần dưới của ảnh.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            Image(
                painter = painterResource(introSlides[pageIndex].image),
                contentDescription = stringResource(introSlides[pageIndex].label),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

            // Nút SKIP ở góc trên bên phải (ẩn ở slide cuối).
            if (!isLast) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    SkipButton(onClick = onFinish)
                }
            }

            // Box đáy: bo 2 góc trên, đè lên ảnh — chứa tiêu đề, chấm chỉ trang, nút chính.
            val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(IntroButtonSurface, sheetShape)
                    .border(2.dp, IntroBorder, sheetShape)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(slide.label),
                    color = IntroText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    // Giữ 2 dòng cố định để box đáy cao bằng nhau ở mọi slide (kể cả tiêu đề dài).
                    minLines = 2,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                PageIndicator(
                    count = introSlides.size,
                    current = page,
                    accent = slide.accent
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(
                        if (isLast) R.string.intro_get_started else R.string.intro_next
                    ),
                    onClick = {
                        if (isLast) {
                            onFinish()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(page + 1) }
                        }
                    }
                )
            }
        }
    }

/** Nút chính Neo-Brutalism, full width, có hiệu ứng "thụt xuống" khi nhấn. */
@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // Khi nghỉ: mặt nút ở trên-trái, lộ bóng dưới-phải. Khi nhấn: mặt nút trượt
    // xuống-phải đè lên bóng (hiệu ứng "sunk").
    val sink by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        label = "primarySink"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Lớp bóng cố định ở dưới-phải.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, top = 4.dp)
                .background(IntroShadow, shape)
        )
        // Lớp mặt nút, trượt theo [sink].
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = sink, top = sink, end = 4.dp - sink, bottom = 4.dp - sink)
                .background(IntroButtonSurface, shape)
                .border(2.dp, IntroBorder, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = IntroText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

/** Nút SKIP gọn ở góc trên, kiểu viền Neo-Brutalism. */
@Composable
private fun SkipButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(IntroButtonSurface, RoundedCornerShape(10.dp))
            .border(2.dp, IntroBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.intro_skip),
            color = IntroText,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/** Chấm chỉ trang: ô vuông bo góc, ô đang chọn dài và tô màu accent. */
@Composable
private fun PageIndicator(
    count: Int,
    current: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(if (selected) 28.dp else 12.dp)
                    .background(
                        if (selected) accent else IntroButtonSurface,
                        RoundedCornerShape(4.dp)
                    )
                    .border(2.dp, IntroBorder, RoundedCornerShape(4.dp))
            )
        }
    }
}
