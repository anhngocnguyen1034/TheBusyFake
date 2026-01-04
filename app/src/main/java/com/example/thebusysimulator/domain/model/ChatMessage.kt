package com.example.thebusysimulator.domain.model

import java.util.Date

data class ChatMessage(
    val id: String,
    val messageId: String,
    val text: String,
    val timestamp: Date,
    val isFromMe: Boolean
)

