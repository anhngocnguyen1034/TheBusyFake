package com.example.thebusysimulator.data.dao

import androidx.room.*
import com.example.thebusysimulator.data.model.FakeCallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FakeCallDao {
    @Query("SELECT * FROM fake_calls ORDER BY scheduledTime DESC")
    fun getAllCalls(): Flow<List<FakeCallEntity>>
    
    @Query("SELECT * FROM fake_calls WHERE id = :id")
    suspend fun getCallById(id: String): FakeCallEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: FakeCallEntity)
    
    @Update
    suspend fun updateCall(call: FakeCallEntity)
    
    @Query("DELETE FROM fake_calls WHERE id = :id")
    suspend fun deleteCallById(id: String)
    
    @Query("SELECT * FROM fake_calls WHERE isCompleted = 1 ORDER BY scheduledTime DESC")
    fun getCompletedCalls(): Flow<List<FakeCallEntity>>
    
    @Query("SELECT * FROM fake_calls WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getScheduledCalls(): Flow<List<FakeCallEntity>>
}
