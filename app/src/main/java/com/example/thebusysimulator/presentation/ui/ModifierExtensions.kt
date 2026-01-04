package com.example.thebusysimulator.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Extension function để thêm padding cho status bar
 * Sử dụng windowInsetsPadding để tự động xử lý padding chính xác
 */
@Composable
fun Modifier.statusBarPadding(): Modifier {
    return this.windowInsetsPadding(WindowInsets.statusBars)
}

/**
 * Extension function để thêm padding cho navigation bar
 */
@Composable
fun Modifier.navigationBarPadding(): Modifier {
    return this.windowInsetsPadding(WindowInsets.navigationBars)
}

/**
 * Extension function để thêm padding cho cả status bar và navigation bar
 */
@Composable
fun Modifier.systemBarsPadding(): Modifier {
    return this.windowInsetsPadding(WindowInsets.systemBars)
}

