package com.example.thebusysimulator.data.mapper

import com.example.thebusysimulator.data.model.MessageEntity
import com.example.thebusysimulator.domain.model.Message
import java.util.Date

object MessageMapper {
    fun toEntity(data: MessageEntity): Message {
        return Message(
            id = data.id,
            contactName = data.contactName,
            lastMessage = data.lastMessage,
            timestamp = Date(data.timestamp),
            unreadCount = data.unreadCount,
            avatarUri = data.avatarUri,
            isVerified = data.isVerified
        )
    }
    
    fun fromEntity(entity: Message): MessageEntity {
        return MessageEntity(
            id = entity.id,
            contactName = entity.contactName,
            lastMessage = entity.lastMessage,
            timestamp = entity.timestamp.time,
            unreadCount = entity.unreadCount,
            avatarUri = entity.avatarUri,
            isVerified = entity.isVerified
        )
    }
}

