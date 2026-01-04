package com.example.thebusysimulator.data.dao

import androidx.room.*
import com.example.thebusysimulator.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)
    
    @Query("UPDATE messages SET lastMessage = :lastMessage, timestamp = :timestamp, unreadCount = :unreadCount WHERE id = :id")
    suspend fun updateLastMessage(id: String, lastMessage: String, timestamp: Long, unreadCount: Int)
}

