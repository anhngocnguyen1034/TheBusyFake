package com.example.thebusysimulator.data.repository

import com.example.thebusysimulator.data.dao.ChatMessageDao
import com.example.thebusysimulator.data.dao.MessageDao
import com.example.thebusysimulator.data.mapper.ChatMessageMapper
import com.example.thebusysimulator.data.mapper.MessageMapper
import com.example.thebusysimulator.domain.model.ChatMessage
import com.example.thebusysimulator.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(
    private val messageDao: MessageDao,
    private val chatMessageDao: ChatMessageDao
) {
    fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { MessageMapper.toEntity(it) }
        }
    }
    
    suspend fun getMessageById(id: String): Message? {
        return messageDao.getMessageById(id)?.let { MessageMapper.toEntity(it) }
    }
    
    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(MessageMapper.fromEntity(message))
    }
    
    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(MessageMapper.fromEntity(message))
    }
    
    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessageById(id)
        chatMessageDao.deleteChatMessagesByMessageId(id)
    }
    
    suspend fun updateLastMessage(id: String, lastMessage: String, timestamp: Long, unreadCount: Int) {
        messageDao.updateLastMessage(id, lastMessage, timestamp, unreadCount)
    }
    
    fun getChatMessagesByMessageId(messageId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getChatMessagesByMessageId(messageId).map { entities ->
            entities.map { ChatMessageMapper.toEntity(it) }
        }
    }
    
    suspend fun insertChatMessage(chatMessage: ChatMessage) {
        chatMessageDao.insertChatMessage(ChatMessageMapper.fromEntity(chatMessage))
        // Update the last message in the parent message
        val message = getMessageById(chatMessage.messageId)
        updateLastMessage(
            id = chatMessage.messageId,
            lastMessage = chatMessage.text,
            timestamp = chatMessage.timestamp.time,
            unreadCount = if (chatMessage.isFromMe) 0 else (message?.unreadCount ?: 0) + 1
        )
    }
    
    suspend fun deleteChatMessage(chatMessageId: String) {
        chatMessageDao.deleteChatMessageById(chatMessageId)
    }
    
    suspend fun updateChatMessage(chatMessage: ChatMessage) {
        chatMessageDao.updateChatMessage(ChatMessageMapper.fromEntity(chatMessage))
    }
}

