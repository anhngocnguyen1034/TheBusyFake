package com.example.thebusysimulator.data.dao

import androidx.room.*
import com.example.thebusysimulator.data.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId ORDER BY timestamp ASC")
    fun getChatMessagesByMessageId(messageId: String): Flow<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessage: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteChatMessagesByMessageId(messageId: String)
}

