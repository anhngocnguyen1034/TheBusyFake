package com.example.thebusysimulator.domain.util

import com.example.thebusysimulator.domain.model.FakeCall

/**
 * Interface for scheduling fake calls
 */
interface CallScheduler {
    fun schedule(fakeCall: FakeCall)
    fun cancel(callId: String)
    fun testCallNow(callerName: String, callerNumber: String, isVideoCall: Boolean = false)
}


