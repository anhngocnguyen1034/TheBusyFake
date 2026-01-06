package com.example.thebusysimulator.domain.model

import java.util.Date

/**
 * Domain entity representing a fake call
 */
data class FakeCall(
    val id: String,
    val callerName: String,
    val callerNumber: String,
    val scheduledTime: Date,
    val isCompleted: Boolean = false,
    val isVideoCall: Boolean = false
)






