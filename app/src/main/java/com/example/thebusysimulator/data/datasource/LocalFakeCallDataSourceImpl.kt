package com.example.thebusysimulator.data.datasource

import com.example.thebusysimulator.data.model.FakeCallData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory implementation of LocalFakeCallDataSource
 * In production, this should be replaced with Room or SharedPreferences
 */
class LocalFakeCallDataSourceImpl : LocalFakeCallDataSource {
    private val _calls = MutableStateFlow<Map<String, FakeCallData>>(emptyMap())
    private val calls: StateFlow<Map<String, FakeCallData>> = _calls

    override suspend fun saveCall(call: FakeCallData): Result<Unit> {
        return try {
            val currentCalls = _calls.value.toMutableMap()
            currentCalls[call.id] = call
            _calls.value = currentCalls
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCall(callId: String): Result<Unit> {
        return try {
            val currentCalls = _calls.value.toMutableMap()
            currentCalls.remove(callId)
            _calls.value = currentCalls
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCallById(callId: String): Result<FakeCallData?> {
        return try {
            Result.success(_calls.value[callId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCalls(): Result<List<FakeCallData>> {
        return try {
            Result.success(_calls.value.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCall(call: FakeCallData): Result<Unit> {
        return try {
            val currentCalls = _calls.value.toMutableMap()
            currentCalls[call.id] = call
            _calls.value = currentCalls
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




