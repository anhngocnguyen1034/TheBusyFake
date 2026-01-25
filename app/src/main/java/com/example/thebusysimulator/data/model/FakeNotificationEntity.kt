package com.example.thebusysimulator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fake_notifications")
data class FakeNotificationEntity(
    @PrimaryKey
    val id: String,
    val senderName: String,
    val messageText: String,
    val sentTime: Long, // Timestamp khi notification được gửi
    val isScheduled: Boolean = false // true nếu là scheduled, false nếu là send now
)
