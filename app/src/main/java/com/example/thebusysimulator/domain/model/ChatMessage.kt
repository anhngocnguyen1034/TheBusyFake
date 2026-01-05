package com.example.thebusysimulator.domain.model

import java.util.Date

data class ChatMessage(
    val id: String,
    val messageId: String,
    val text: String,
    val timestamp: Date,
    val isFromMe: Boolean,
    val imageUri: String? = null, // URI của ảnh nếu có
    val replyToMessageId: String? = null // ID của tin nhắn được phản hồi
)

