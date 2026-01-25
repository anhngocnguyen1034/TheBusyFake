package com.example.thebusysimulator.data.dao

import androidx.room.*
import com.example.thebusysimulator.data.model.FakeNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FakeNotificationDao {
    @Query("SELECT * FROM fake_notifications ORDER BY sentTime DESC")
    fun getAllNotifications(): Flow<List<FakeNotificationEntity>>
    
    @Query("SELECT * FROM fake_notifications WHERE id = :id")
    suspend fun getNotificationById(id: String): FakeNotificationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: FakeNotificationEntity)
    
    @Update
    suspend fun updateNotification(notification: FakeNotificationEntity)
    
    @Query("DELETE FROM fake_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
    
    @Query("SELECT * FROM fake_notifications ORDER BY sentTime DESC")
    fun getNotificationHistory(): Flow<List<FakeNotificationEntity>>
}
