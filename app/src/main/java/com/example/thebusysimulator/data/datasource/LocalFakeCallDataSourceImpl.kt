package com.example.thebusysimulator.data.datasource

import com.example.thebusysimulator.data.dao.FakeCallDao
import com.example.thebusysimulator.data.mapper.FakeCallMapper
import com.example.thebusysimulator.data.model.FakeCallData
import kotlinx.coroutines.flow.first

/**
 * Room database implementation of LocalFakeCallDataSource
 * Uses Room to persist data across app restarts
 */
class LocalFakeCallDataSourceImpl(
    private val fakeCallDao: FakeCallDao,
    private val mapper: FakeCallMapper
) : LocalFakeCallDataSource {

    override suspend fun saveCall(call: FakeCallData): Result<Unit> {
        return try {
            val entity = mapper.mapDataToRoomEntity(call)
            fakeCallDao.insertCall(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCall(callId: String): Result<Unit> {
        return try {
            fakeCallDao.deleteCallById(callId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCallById(callId: String): Result<FakeCallData?> {
        return try {
            val entity = fakeCallDao.getCallById(callId)
            val data = entity?.let {
                FakeCallData(
                    id = it.id,
                    callerName = it.callerName,
                    callerNumber = it.callerNumber,
                    scheduledTime = it.scheduledTime,
                    isCompleted = it.isCompleted,
                    isVideoCall = it.isVideoCall
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCalls(): Result<List<FakeCallData>> {
        return try {
            // Use first() to get the current value from Flow
            val entities = fakeCallDao.getAllCalls().first()
            val data = entities.map {
                FakeCallData(
                    id = it.id,
                    callerName = it.callerName,
                    callerNumber = it.callerNumber,
                    scheduledTime = it.scheduledTime,
                    isCompleted = it.isCompleted,
                    isVideoCall = it.isVideoCall
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCall(call: FakeCallData): Result<Unit> {
        return try {
            val entity = mapper.mapDataToRoomEntity(call)
            fakeCallDao.updateCall(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}







