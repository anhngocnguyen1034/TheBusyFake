package com.example.thebusysimulator.presentation.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.ui.screen.GenZThemeColors
import com.example.thebusysimulator.presentation.ui.screen.getGenZTheme

@Composable
fun GenZBackButton(
    onClick: () -> Unit,
    theme: GenZThemeColors = getGenZTheme()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offset by animateDpAsState(targetValue = if (isPressed) 0.dp else 4.dp, label = "back")

    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(theme.shadow, RoundedCornerShape(8.dp))
        )
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offset, y = offset)
                .background(theme.surface, RoundedCornerShape(8.dp))
                .border(2.dp, theme.border, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                tint = theme.text,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
