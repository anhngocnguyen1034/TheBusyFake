package com.example.thebusysimulator.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["messageId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val messageId: String, // Foreign key to MessageEntity
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val imageUri: String? = null, // URI của ảnh nếu có
    val replyToMessageId: String? = null // ID của tin nhắn được phản hồi
)

