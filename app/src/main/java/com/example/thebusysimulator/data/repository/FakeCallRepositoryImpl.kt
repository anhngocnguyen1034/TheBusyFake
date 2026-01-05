package com.example.thebusysimulator.data.repository

import com.example.thebusysimulator.data.datasource.LocalFakeCallDataSource
import com.example.thebusysimulator.data.mapper.FakeCallMapper
import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.domain.repository.FakeCallRepository

/**
 * Repository implementation for fake calls
 */
class FakeCallRepositoryImpl(
    private val localDataSource: LocalFakeCallDataSource,
    private val mapper: FakeCallMapper
) : FakeCallRepository {

    override suspend fun scheduleCall(fakeCall: FakeCall): Result<FakeCall> {
        return try {
            val callData = mapper.mapFromEntity(fakeCall)
            localDataSource.saveCall(callData).getOrElse {
                return Result.failure(it)
            }
            Result.success(fakeCall)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelCall(callId: String): Result<Unit> {
        return try {
            localDataSource.deleteCall(callId).getOrElse {
                return Result.failure(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getScheduledCalls(): Result<List<FakeCall>> {
        return try {
            val callsData = localDataSource.getAllCalls().getOrElse {
                return Result.failure(it)
            }
            val calls = callsData.map { mapper.mapToEntity(it) }
            Result.success(calls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCallById(callId: String): Result<FakeCall> {
        return try {
            val callData = localDataSource.getCallById(callId).getOrElse {
                return Result.failure(it)
            }
            callData?.let {
                Result.success(mapper.mapToEntity(it))
            } ?: Result.failure(IllegalArgumentException("Call not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markCallAsCompleted(callId: String): Result<Unit> {
        return try {
            val callData = localDataSource.getCallById(callId).getOrElse {
                return Result.failure(it)
            }
            callData?.let {
                val updatedCall = it.copy(isCompleted = true)
                localDataSource.updateCall(updatedCall).getOrElse {
                    return Result.failure(it)
                }
                Result.success(Unit)
            } ?: Result.failure(IllegalArgumentException("Call not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




