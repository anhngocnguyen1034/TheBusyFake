package com.example.thebusysimulator.domain.model

import java.util.Date

data class Message(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: Date,
    val unreadCount: Int = 0,
    val avatarUri: String? = null,
    val isVerified: Boolean = false
)

