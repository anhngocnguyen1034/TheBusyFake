package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thebusysimulator.data.datasource.LanguageDataSource
import com.example.thebusysimulator.presentation.util.LanguageManager
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val languageDataSource = remember { LanguageDataSource(context) }
    val scope = rememberCoroutineScope()

    var currentLanguageCode by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentLanguageCode = languageDataSource.languageCode.first()
    }

    val colorScheme = MaterialTheme.colorScheme
    val languages = LanguageManager.getSupportedLanguages()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.background, colorScheme.surfaceVariant) // Đổi surfaceVariant cho dịu mắt hơn chút
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarPadding()
        ) {
            // Header được căn chỉnh lại một chút cho thoáng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(languages) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = currentLanguageCode == language.code,
                        onClick = {
                            scope.launch {
                                languageDataSource.setLanguageCode(language.code)
                                currentLanguageCode = language.code
                                LanguageManager.setLanguage(context, language.code)
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: LanguageManager.Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Animation cho màu nền
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            colorScheme.primaryContainer // Màu nổi bật khi chọn
        } else {
            colorScheme.surface.copy(alpha = 0.3f) // Trong suốt nhẹ khi chưa chọn (thay vì White cứng)
        },
        label = "colorAnim"
    )

    // Animation cho độ cao (bóng) -> Quan trọng: Unselected = 0.dp
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        label = "elevationAnim"
    )

    // Animation cho viền
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else colorScheme.outline.copy(alpha = 0.3f),
        label = "borderAnim"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        // Chỉ hiện bóng khi được chọn, chưa chọn thì phẳng lì
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        // Thêm viền (Border) khi chưa chọn để tạo hình khối rõ ràng mà không cần bóng
        border = if (!isSelected) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp), // Tăng padding dọc cho thoáng
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold, // Đậm hơn khi chọn
                    color = if (isSelected) {
                        colorScheme.onPrimaryContainer
                    } else {
                        colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.code, // Có thể format lại ví dụ: English (en)
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        colorScheme.onSurfaceVariant
                    }
                )
            }

            // Icon checkmark
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected",
                    tint = colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = colorScheme.background.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        ) // Thêm background tròn nhỏ sau icon check cho nổi
                        .padding(4.dp)
                )
            }
        }
    }
}