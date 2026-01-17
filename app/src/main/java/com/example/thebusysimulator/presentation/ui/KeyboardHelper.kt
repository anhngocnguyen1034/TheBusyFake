package com.example.thebusysimulator.presentation.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Hàm chung để ẩn bàn phím và clear focus của TextField
 * Sử dụng cả SoftwareKeyboardController và FocusManager để đảm bảo
 * bàn phím ẩn và con trỏ nháy (cursor) cũng biến mất
 */
@Composable
fun hideKeyboardAndClearFocus() {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    keyboardController?.hide()
    focusManager.clearFocus()
}

/**
 * Extension function để thêm modifier ẩn bàn phím và clear focus khi click vào màn hình
 * Sử dụng cho Box, Column, Row hoặc các container khác
 */
@Composable
fun Modifier.hideKeyboardOnClick(): Modifier {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }
}
