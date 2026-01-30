package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import com.example.thebusysimulator.presentation.ui.theme.DarkBackground
import com.example.thebusysimulator.presentation.ui.theme.DarkBorder
import com.example.thebusysimulator.presentation.ui.theme.DarkSurface
import com.example.thebusysimulator.presentation.ui.theme.DarkText
import com.example.thebusysimulator.presentation.ui.theme.LightBackground
import com.example.thebusysimulator.presentation.ui.theme.LightBorder
import com.example.thebusysimulator.presentation.ui.theme.LightSurface
import com.example.thebusysimulator.presentation.ui.theme.LightText

@Composable
fun getGenZTheme(): GenZThemeColors {

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f

    return if (isDark) {
        // DARK MODE
        GenZThemeColors(
            background = DarkBackground,
            surface = DarkSurface,
            border = DarkBorder,
            text = DarkText,
            shadow = DarkBorder.copy(alpha = 0.3f),
            pattern = DarkText.copy(alpha = 0.1f)
        )
    } else {
        // LIGHT MODE
        GenZThemeColors(
            background = LightBackground,
            surface = LightSurface,
            border = LightBorder,
            text = LightText,
            shadow = LightBorder,
            pattern = LightText.copy(alpha = 0.15f)
        )
    }
}