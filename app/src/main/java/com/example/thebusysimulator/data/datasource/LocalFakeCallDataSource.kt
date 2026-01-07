package com.example.thebusysimulator.data.datasource

import com.example.thebusysimulator.data.model.FakeCallData

/**
 * Local data source for storing fake calls
 * Uses in-memory storage (can be replaced with Room, SharedPreferences, etc.)
 */
interface LocalFakeCallDataSource {
    suspend fun saveCall(call: FakeCallData): Result<Unit>
    suspend fun deleteCall(callId: String): Result<Unit>
    suspend fun getCallById(callId: String): Result<FakeCallData?>
    suspend fun getAllCalls(): Result<List<FakeCallData>>
    suspend fun updateCall(call: FakeCallData): Result<Unit>
}







