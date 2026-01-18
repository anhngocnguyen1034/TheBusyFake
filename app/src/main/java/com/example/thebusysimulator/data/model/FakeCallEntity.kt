package com.example.thebusysimulator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fake_calls")
data class FakeCallEntity(
    @PrimaryKey
    val id: String,
    val callerName: String,
    val callerNumber: String,
    val scheduledTime: Long, // Timestamp
    val isCompleted: Boolean = false,
    val isVideoCall: Boolean = false
)
