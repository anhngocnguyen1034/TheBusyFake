package com.example.thebusysimulator.domain.model

import java.util.Date

/**
 * Domain entity representing a fake notification
 */
data class FakeNotification(
    val id: String,
    val senderName: String,
    val messageText: String,
    val sentTime: Date,
    val isScheduled: Boolean = false
)
