package com.example.thebusysimulator.data.model

/**
 * Data model for fake call
 * Used for local storage
 */
data class FakeCallData(
    val id: String,
    val callerName: String,
    val callerNumber: String,
    val scheduledTime: Long, // Timestamp
    val isCompleted: Boolean = false,
    val isVideoCall: Boolean = false
)


