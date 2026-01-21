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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. MATERIAL COLOR SCHEMES
private val DarkColorScheme = darkColorScheme(
    primary = CyberNeonPurple,
    secondary = CyberNeonCyan,
    tertiary = CyberNeonPink,
    background = CyberDarkBackground,
    surface = CyberDarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = CyberDarkText,
    onSurface = CyberDarkText,
    surfaceVariant = CyberDarkSurface,
    onSurfaceVariant = DarkIconInactive
)

private val LightColorScheme = lightColorScheme(
    primary = CyberNeonPurple,
    secondary = CyberNeonCyan,
    tertiary = CyberNeonPink,
    background = CyberLightBackground,
    surface = CyberLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = CyberLightText,
    onSurface = CyberLightText,
    surfaceVariant = CyberLightSurface,
    onSurfaceVariant = LightIconInactive
)

// 2. CUSTOM GEN Z COLORS (Đã thêm neonBlue/Yellow/Pink)
@Immutable
data class GenZColors(
    val border: Color,
    val shadow: Color,
    val pattern: Color,
    val text: Color,
    val neonBlue: Color = CyberNeonCyan,   // Sửa lỗi Unresolved reference neonBlue
    val neonYellow: Color = CyberNeonYellow, // Sửa lỗi Unresolved reference neonYellow
    val neonPink: Color = CyberNeonPink
)

private val LightGenZColors = GenZColors(
    border = CyberLightBorder,
    shadow = CyberLightShadow,
    pattern = Color.Black.copy(alpha = 0.05f),
    text = CyberLightText
)

private val DarkGenZColors = GenZColors(
    border = CyberDarkBorder,
    shadow = CyberDarkShadow,
    pattern = Color.White.copy(alpha = 0.05f),
    text = CyberDarkText
)

val LocalGenZColors = staticCompositionLocalOf { LightGenZColors }

object GenZTheme {
    val colors: GenZColors
        @Composable
        get() = LocalGenZColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        get() = MaterialTheme.typography
}

// 3. THEME COMPOSABLES
@Composable
fun TheBusySimulatorTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }
    // Force disable dynamic color for this theme style usually
    val useDynamicColor = dynamicColor && themeMode == "system"

    TheBusySimulatorThemeInternal(
        darkTheme = darkTheme,
        dynamicColor = useDynamicColor,
        content = content
    )
}

@Composable
private fun TheBusySimulatorThemeInternal(
    darkTheme: Boolean,
    dynamicColor: Boolean,
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

    val genZColors = if (darkTheme) DarkGenZColors else LightGenZColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGenZColors provides genZColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}