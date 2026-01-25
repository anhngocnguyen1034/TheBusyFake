package com.example.thebusysimulator.data.model

/**
 * Data model for fake notification
 */
data class FakeNotificationData(
    val id: String,
    val senderName: String,
    val messageText: String,
    val sentTime: Long,
    val isScheduled: Boolean = false
)
