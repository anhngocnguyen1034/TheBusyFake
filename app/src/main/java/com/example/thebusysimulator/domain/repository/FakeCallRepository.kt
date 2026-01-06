package com.example.thebusysimulator.domain.repository

import com.example.thebusysimulator.domain.model.FakeCall

/**
 * Repository interface for managing fake calls
 */
interface FakeCallRepository {
    suspend fun scheduleCall(fakeCall: FakeCall): Result<FakeCall>
    suspend fun cancelCall(callId: String): Result<Unit>
    suspend fun getScheduledCalls(): Result<List<FakeCall>>
    suspend fun getCallById(callId: String): Result<FakeCall>
    suspend fun markCallAsCompleted(callId: String): Result<Unit>
}





