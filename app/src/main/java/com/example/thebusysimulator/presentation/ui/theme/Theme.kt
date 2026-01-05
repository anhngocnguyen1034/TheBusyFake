package com.example.thebusysimulator.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ===== CYBER GLITCH DARK MODE =====
// Phong cách Hacker/Gamer - Màu đen sâu với Neon rực rỡ
private val DarkColorScheme = darkColorScheme(
    primary = CyberDarkPrimary,           // #D900FF - Tím Neon
    secondary = CyberDarkAccent,          // #00FFC2 - Xanh Cyan Neon
    tertiary = CyberDarkAccent,           // Dùng accent cho tertiary
    background = CyberDarkBackground,      // #050505 - Đen gần như tuyệt đối
    surface = CyberDarkSurface,           // #151515 - Đen nhám cho Card
    onPrimary = Color.White,               // Trắng trên nút tím
    onSecondary = Color.Black,             // Đen trên nút xanh (để nổi bật)
    onBackground = CyberDarkText,         // #FFFFFF - Trắng tinh
    onSurface = CyberDarkText,             // #FFFFFF - Trắng tinh
    surfaceVariant = CyberDarkSurface,     // #151515
    onSurfaceVariant = DarkIconInactive    // Xám cho icon không active
)

// ===== CYBER GLITCH LIGHT MODE =====
// Phong cách nhẹ nhàng hơn cho Light Mode
private val LightColorScheme = lightColorScheme(
    primary = CyberLightPrimary,           // #6C5CE7 - Tím đậm
    secondary = CyberLightAccent,          // #00CEC9 - Xanh ngọc đậm
    tertiary = CyberLightAccent,          // Dùng accent cho tertiary
    background = CyberLightBackground,     // #F0F0F3 - Xám trắng nhẹ
    surface = CyberLightSurface,           // #FFFFFF - Trắng tinh
    onPrimary = Color.White,               // Trắng trên nút tím
    onSecondary = Color.White,             // Trắng trên nút xanh
    onBackground = CyberLightText,         // #2D3436 - Xám đen
    onSurface = CyberLightText,             // #2D3436 - Xám đen
    surfaceVariant = CyberLightSurface,    // #FFFFFF
    onSurfaceVariant = LightIconInactive   // Xám cho icon không active
)

@Composable
fun TheBusySimulatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Cấu hình status bar để màu chữ thay đổi theo theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                // Dark theme: màu chữ trắng (light icons)
                // Light theme: màu chữ đen (dark icons)
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun TheBusySimulatorTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme // "system"
    }
    
    // Disable dynamic color when user explicitly selects a theme (not "system")
    // This ensures our custom colors are used
    val shouldUseDynamicColor = dynamicColor && themeMode == "system"
    
    // Cấu hình status bar sẽ được thực hiện trong hàm TheBusySimulatorTheme(darkTheme)
    TheBusySimulatorTheme(
        darkTheme = darkTheme,
        dynamicColor = shouldUseDynamicColor,
        content = content
    )
}


