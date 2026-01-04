package com.example.thebusysimulator.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thebusysimulator.data.repository.MessageRepository
import com.example.thebusysimulator.domain.model.ChatMessage
import com.example.thebusysimulator.domain.model.Message
import com.example.thebusysimulator.presentation.util.AutoReplyHelper
import com.example.thebusysimulator.presentation.util.ChatTimeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.Calendar

class MessageViewModel(
    private val messageRepository: MessageRepository,
    private val context: Context? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()
    
    private val _chatUiState = MutableStateFlow<Map<String, ChatUiState>>(emptyMap())
    val chatUiState: StateFlow<Map<String, ChatUiState>> = _chatUiState.asStateFlow()
    
    private val prefs: SharedPreferences? = context?.getSharedPreferences("auto_reply_prefs", Context.MODE_PRIVATE)
    
    init {
        loadMessages()
        viewModelScope.launch {
            // Đợi một chút để messages được load
            delay(1000)
            initializeDefaultMomMessages()
            initializeDefaultLoverMessages()
        }
    }
    
    private suspend fun initializeDefaultMomMessages() {
        try {
            // Kiểm tra xem đã khởi tạo tin nhắn mẹ chưa bằng SharedPreferences
            val isInitialized = prefs?.getBoolean("mom_messages_initialized", false) ?: false
            if (isInitialized) {
                return // Đã khởi tạo rồi, không tạo lại
            }
            
            // Kiểm tra xem đã có tin nhắn mẹ trong database chưa
            val currentMessages = _uiState.value.messages
            val momMessage = currentMessages.find { AutoReplyHelper.isMomContact(it.contactName) }
            
            if (momMessage == null) {
                // Tạo tin nhắn "Mẹ" mặc định
                val momId = UUID.randomUUID().toString()
                val defaultMessage = Message(
                    id = momId,
                    contactName = "Mẹ",
                    lastMessage = AutoReplyHelper.defaultMomMessages.first(),
                    timestamp = Date(),
                    unreadCount = AutoReplyHelper.defaultMomMessages.size,
                    avatarUri = null
                )
                messageRepository.insertMessage(defaultMessage)
                
                // Tạo các tin nhắn mặc định từ "Mẹ"
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, -2) // Tin nhắn đầu cách đây 2 giờ
                
                AutoReplyHelper.defaultMomMessages.forEachIndexed { index, text ->
                    calendar.add(Calendar.MINUTE, 30) // Mỗi tin nhắn cách nhau 30 phút
                    val chatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        messageId = momId,
                        text = text,
                        timestamp = calendar.time,
                        isFromMe = false
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Khởi tạo reply index
                prefs?.edit()?.putInt("reply_index_$momId", 0)?.apply()
                
                // Đánh dấu đã khởi tạo để không tạo lại lần sau
                prefs?.edit()?.putBoolean("mom_messages_initialized", true)?.apply()
            } else {
                // Nếu đã có tin nhắn mẹ trong database, cũng đánh dấu đã khởi tạo
                prefs?.edit()?.putBoolean("mom_messages_initialized", true)?.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeDefaultLoverMessages() {
        try {
            // Kiểm tra xem đã khởi tạo tin nhắn người yêu chưa bằng SharedPreferences
            val isInitialized = prefs?.getBoolean("lover_messages_initialized", false) ?: false
            if (isInitialized) {
                return // Đã khởi tạo rồi, không tạo lại
            }
            
            // Kiểm tra xem đã có tin nhắn người yêu trong database chưa
            val currentMessages = _uiState.value.messages
            val loverMessage = currentMessages.find { AutoReplyHelper.isLoverContact(it.contactName) }
            
            if (loverMessage == null) {
                // Tạo tin nhắn "Người yêu" mặc định
                val loverId = UUID.randomUUID().toString()
                val defaultMessage = Message(
                    id = loverId,
                    contactName = "Người yêu",
                    lastMessage = AutoReplyHelper.defaultLoverMessages.first(),
                    timestamp = Date(),
                    unreadCount = AutoReplyHelper.defaultLoverMessages.size,
                    avatarUri = null
                )
                messageRepository.insertMessage(defaultMessage)
                
                // Tạo các tin nhắn mặc định từ "Người yêu"
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, -1) // Tin nhắn đầu cách đây 1 giờ
                
                AutoReplyHelper.defaultLoverMessages.forEachIndexed { index, text ->
                    calendar.add(Calendar.MINUTE, 20) // Mỗi tin nhắn cách nhau 20 phút
                    val chatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        messageId = loverId,
                        text = text,
                        timestamp = calendar.time,
                        isFromMe = false
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Khởi tạo reply index
                prefs?.edit()?.putInt("reply_index_$loverId", 0)?.apply()
                
                // Đánh dấu đã khởi tạo để không tạo lại lần sau
                prefs?.edit()?.putBoolean("lover_messages_initialized", true)?.apply()
            } else {
                // Nếu đã có tin nhắn người yêu trong database, cũng đánh dấu đã khởi tạo
                prefs?.edit()?.putBoolean("lover_messages_initialized", true)?.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun loadMessages() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                messageRepository.getAllMessages().collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
    
    fun loadChatMessages(messageId: String) {
        viewModelScope.launch {
            try {
                val currentState = _chatUiState.value.toMutableMap()
                currentState[messageId] = ChatUiState(isLoading = true, isTyping = false)
                _chatUiState.value = currentState
                
                messageRepository.getChatMessagesByMessageId(messageId).collect { chatMessages ->
                    val updatedState = _chatUiState.value.toMutableMap()
                    val currentState = updatedState[messageId]
                    updatedState[messageId] = ChatUiState(
                        chatMessages = chatMessages,
                        isLoading = false,
                        errorMessage = null,
                        isTyping = currentState?.isTyping ?: false
                    )
                    _chatUiState.value = updatedState
                }
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[messageId]
                currentState[messageId] = ChatUiState(
                    isLoading = false,
                    errorMessage = e.message,
                    isTyping = existingState?.isTyping ?: false
                )
                _chatUiState.value = currentState
            }
        }
    }
    
    fun addMessage(contactName: String, avatarUri: String?) {
        viewModelScope.launch {
            try {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    contactName = contactName,
                    lastMessage = "",
                    timestamp = Date(),
                    unreadCount = 0,
                    avatarUri = avatarUri
                )
                messageRepository.insertMessage(message)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.deleteMessage(messageId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    fun sendChatMessage(messageId: String, text: String) {
        viewModelScope.launch {
            try {
                // Gửi tin nhắn của người dùng
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    messageId = messageId,
                    text = text,
                    timestamp = Date(),
                    isFromMe = true
                )
                messageRepository.insertChatMessage(chatMessage)
                
                // Tự động trả lời cho tất cả các contact (Mẹ, Người yêu, và các contact tự tạo)
                val message = messageRepository.getMessageById(messageId)
                if (message != null) {
                    val isMom = AutoReplyHelper.isMomContact(message.contactName)
                    val isLover = AutoReplyHelper.isLoverContact(message.contactName)
                    
                    // Lấy reply index hiện tại
                    val currentIndex = prefs?.getInt("reply_index_$messageId", 0) ?: 0
                    
                    // Lấy tin nhắn trả lời tương ứng
                    val replyText = when {
                        isMom -> AutoReplyHelper.getNextReply(currentIndex)
                        isLover -> AutoReplyHelper.getNextLoverReply(currentIndex)
                        else -> AutoReplyHelper.getNextGenericReply(currentIndex) // Contact tự tạo
                    }
                    
                    // Xác định loại nhân vật và tính toán thời gian delay
                    val characterType = AutoReplyHelper.getCharacterType(message.contactName)
                    val delayTime = ChatTimeCalculator.calculateDelay(replyText, characterType)
                    
                    // Bật typing indicator
                    val currentState = _chatUiState.value.toMutableMap()
                    currentState[messageId] = currentState[messageId]?.copy(isTyping = true)
                        ?: ChatUiState(isTyping = true)
                    _chatUiState.value = currentState
                    
                    // Đợi thời gian được tính toán dựa trên độ dài tin nhắn và loại nhân vật
                    delay(delayTime)
                    
                    // Tắt typing indicator
                    val updatedState = _chatUiState.value.toMutableMap()
                    updatedState[messageId] = updatedState[messageId]?.copy(isTyping = false)
                        ?: ChatUiState(isTyping = false)
                    _chatUiState.value = updatedState
                    
                    // Tạo tin nhắn trả lời tự động
                    val replyMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        messageId = messageId,
                        text = replyText,
                        timestamp = Date(),
                        isFromMe = false
                    )
                    messageRepository.insertChatMessage(replyMessage)
                    
                    // Cập nhật reply index cho lần sau
                    val totalReplies = when {
                        isMom -> AutoReplyHelper.getTotalReplies()
                        isLover -> AutoReplyHelper.getTotalLoverReplies()
                        else -> AutoReplyHelper.getTotalGenericReplies() // Contact tự tạo
                    }
                    val nextIndex = (currentIndex + 1) % totalReplies
                    prefs?.edit()?.putInt("reply_index_$messageId", nextIndex)?.apply()
                }
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[messageId]
                currentState[messageId] = existingState?.copy(errorMessage = e.message)
                    ?: ChatUiState(errorMessage = e.message, isTyping = false)
                _chatUiState.value = currentState
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class MessageUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ChatUiState(
    val chatMessages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTyping: Boolean = false
)

