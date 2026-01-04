package com.example.thebusysimulator.data.mapper

import com.example.thebusysimulator.data.model.ChatMessageEntity
import com.example.thebusysimulator.domain.model.ChatMessage
import java.util.Date

object ChatMessageMapper {
    fun toEntity(data: ChatMessageEntity): ChatMessage {
        return ChatMessage(
            id = data.id,
            messageId = data.messageId,
            text = data.text,
            timestamp = Date(data.timestamp),
            isFromMe = data.isFromMe
        )
    }
    
    fun fromEntity(entity: ChatMessage): ChatMessageEntity {
        return ChatMessageEntity(
            id = entity.id,
            messageId = entity.messageId,
            text = entity.text,
            timestamp = entity.timestamp.time,
            isFromMe = entity.isFromMe
        )
    }
}

