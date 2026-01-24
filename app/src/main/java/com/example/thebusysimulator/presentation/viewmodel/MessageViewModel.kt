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
import com.example.thebusysimulator.presentation.util.CharacterType
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
            initializeDefaultDoctorMessages()
            initializeDefaultScientistMessages()
            // Reload messages sau khi khởi tạo xong để đảm bảo các contact mặc định được hiển thị
            delay(500)
            loadMessages()
        }
    }
    
    private suspend fun initializeDefaultMomMessages() {
        try {
            // Kiểm tra xem đã khởi tạo tin nhắn mẹ chưa bằng SharedPreferences
            val isInitialized = prefs?.getBoolean("mom_messages_initialized", false) ?: false
            if (isInitialized) {
                return // Đã khởi tạo rồi, không tạo lại
            }
            
            // Kiểm tra xem đã có tin nhắn mẹ trong database chưa bằng messageId đã lưu
            val momMessageId = prefs?.getString("mom_message_id", null)
            val momMessage = if (momMessageId != null) {
                _uiState.value.messages.find { it.id == momMessageId }
            } else {
                null
            }
            
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
                        isFromMe = false,
                        imageUri = null,
                        replyToMessageId = null
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Lưu messageId và loại contact
                prefs?.edit()?.apply {
                    putString("mom_message_id", momId)
                    putString("contact_type_$momId", "mom")
                    putInt("reply_index_$momId", 0)
                    putBoolean("mom_messages_initialized", true)
                }?.apply()
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
            
            // Kiểm tra xem đã có tin nhắn người yêu trong database chưa bằng messageId đã lưu
            val loverMessageId = prefs?.getString("lover_message_id", null)
            val loverMessage = if (loverMessageId != null) {
                _uiState.value.messages.find { it.id == loverMessageId }
            } else {
                null
            }
            
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
                        isFromMe = false,
                        imageUri = null,
                        replyToMessageId = null
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Lưu messageId và loại contact
                prefs?.edit()?.apply {
                    putString("lover_message_id", loverId)
                    putString("contact_type_$loverId", "lover")
                    putInt("reply_index_$loverId", 0)
                    putBoolean("lover_messages_initialized", true)
                }?.apply()
            } else {
                // Nếu đã có tin nhắn người yêu trong database, cũng đánh dấu đã khởi tạo
                prefs?.edit()?.putBoolean("lover_messages_initialized", true)?.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeDefaultDoctorMessages() {
        try {
            // Kiểm tra xem đã khởi tạo tin nhắn bác sĩ chưa bằng SharedPreferences
            val isInitialized = prefs?.getBoolean("doctor_messages_initialized", false) ?: false
            if (isInitialized) {
                return // Đã khởi tạo rồi, không tạo lại
            }
            
            // Kiểm tra xem đã có tin nhắn bác sĩ trong database chưa bằng messageId đã lưu
            val doctorMessageId = prefs?.getString("doctor_message_id", null)
            val doctorMessage = if (doctorMessageId != null) {
                _uiState.value.messages.find { it.id == doctorMessageId }
            } else {
                null
            }
            
            if (doctorMessage == null) {
                // Tạo tin nhắn "Bác sĩ" mặc định
                val doctorId = UUID.randomUUID().toString()
                val defaultMessage = Message(
                    id = doctorId,
                    contactName = "Bác sĩ",
                    lastMessage = AutoReplyHelper.defaultDoctorMessages.first(),
                    timestamp = Date(),
                    unreadCount = AutoReplyHelper.defaultDoctorMessages.size,
                    avatarUri = null
                )
                messageRepository.insertMessage(defaultMessage)
                
                // Tạo các tin nhắn mặc định từ "Bác sĩ"
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, -1) // Tin nhắn đầu cách đây 1 giờ
                
                AutoReplyHelper.defaultDoctorMessages.forEachIndexed { index, text ->
                    calendar.add(Calendar.MINUTE, 15) // Mỗi tin nhắn cách nhau 15 phút
                    val chatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        messageId = doctorId,
                        text = text,
                        timestamp = calendar.time,
                        isFromMe = false,
                        imageUri = null,
                        replyToMessageId = null
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Lưu messageId và loại contact
                prefs?.edit()?.apply {
                    putString("doctor_message_id", doctorId)
                    putString("contact_type_$doctorId", "doctor")
                    putInt("reply_index_$doctorId", 0)
                    putBoolean("doctor_messages_initialized", true)
                }?.apply()
            } else {
                // Nếu đã có tin nhắn bác sĩ trong database, cũng đánh dấu đã khởi tạo
                prefs?.edit()?.putBoolean("doctor_messages_initialized", true)?.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeDefaultScientistMessages() {
        try {
            // Kiểm tra xem đã khởi tạo tin nhắn nhà khoa học chưa bằng SharedPreferences
            val isInitialized = prefs?.getBoolean("scientist_messages_initialized", false) ?: false
            if (isInitialized) {
                return // Đã khởi tạo rồi, không tạo lại
            }
            
            // Kiểm tra xem đã có tin nhắn nhà khoa học trong database chưa bằng messageId đã lưu
            val scientistMessageId = prefs?.getString("scientist_message_id", null)
            val scientistMessage = if (scientistMessageId != null) {
                _uiState.value.messages.find { it.id == scientistMessageId }
            } else {
                null
            }
            
            if (scientistMessage == null) {
                // Tạo tin nhắn "Nhà khoa học" mặc định
                val scientistId = UUID.randomUUID().toString()
                val defaultMessage = Message(
                    id = scientistId,
                    contactName = "Nhà khoa học",
                    lastMessage = AutoReplyHelper.defaultScientistMessages.first(),
                    timestamp = Date(),
                    unreadCount = AutoReplyHelper.defaultScientistMessages.size,
                    avatarUri = null
                )
                messageRepository.insertMessage(defaultMessage)
                
                // Tạo các tin nhắn mặc định từ "Nhà khoa học"
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR, -1) // Tin nhắn đầu cách đây 1 giờ
                
                AutoReplyHelper.defaultScientistMessages.forEachIndexed { index, text ->
                    calendar.add(Calendar.MINUTE, 15) // Mỗi tin nhắn cách nhau 15 phút
                    val chatMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        messageId = scientistId,
                        text = text,
                        timestamp = calendar.time,
                        isFromMe = false,
                        imageUri = null,
                        replyToMessageId = null
                    )
                    messageRepository.insertChatMessage(chatMessage)
                }
                
                // Lưu messageId và loại contact
                prefs?.edit()?.apply {
                    putString("scientist_message_id", scientistId)
                    putString("contact_type_$scientistId", "scientist")
                    putInt("reply_index_$scientistId", 0)
                    putBoolean("scientist_messages_initialized", true)
                }?.apply()
            } else {
                // Nếu đã có tin nhắn nhà khoa học trong database, cũng đánh dấu đã khởi tạo
                prefs?.edit()?.putBoolean("scientist_messages_initialized", true)?.apply()
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
    
    fun addMessage(contactName: String, avatarUri: String?, isVerified: Boolean = false) {
        viewModelScope.launch {
            try {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    contactName = contactName,
                    lastMessage = "",
                    timestamp = Date(),
                    unreadCount = 0,
                    avatarUri = avatarUri,
                    isVerified = isVerified
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
    
    fun sendChatMessage(messageId: String, text: String, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                // Gửi tin nhắn của người dùng
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    messageId = messageId,
                    text = text,
                    timestamp = Date(),
                    isFromMe = true,
                    imageUri = imageUri,
                    replyToMessageId = null
                )
                messageRepository.insertChatMessage(chatMessage)
                
                // Kiểm tra xem có phải preset message không và tự động phản hồi
                val message = _uiState.value.messages.find { it.id == messageId }
                val contactName = message?.contactName ?: ""
                
                if (contactName in listOf("Mẹ", "Người yêu", "Bác sĩ", "Nhà khoa học")) {
                    // Lấy reply index hiện tại
                    val currentIndex = prefs?.getInt("reply_index_$messageId", 0) ?: 0
                    
                    // Lấy câu trả lời phù hợp
                    val replyText = when (contactName) {
                        "Mẹ" -> AutoReplyHelper.getNextReply(currentIndex)
                        "Người yêu" -> AutoReplyHelper.getNextLoverReply(currentIndex)
                        "Bác sĩ" -> AutoReplyHelper.getNextDoctorReply(currentIndex)
                        "Nhà khoa học" -> AutoReplyHelper.getNextScientistReply(currentIndex)
                        else -> ""
                    }
                    
                    if (replyText.isNotEmpty()) {
                        // Hiển thị typing indicator (3 chấm đang nhập)
                        val currentState = _chatUiState.value.toMutableMap()
                        val existingState = currentState[messageId]
                        currentState[messageId] = existingState?.copy(isTyping = true)
                            ?: ChatUiState(isTyping = true)
                        _chatUiState.value = currentState
                        
                        // Đợi 2-3 giây trước khi phản hồi (tạo cảm giác tự nhiên)
                        delay((2000..3000).random().toLong())
                        
                        // Gửi tin nhắn phản hồi tự động
                        val autoReplyMessage = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            messageId = messageId,
                            text = replyText,
                            timestamp = Date(),
                            isFromMe = false,
                            imageUri = null,
                            replyToMessageId = null
                        )
                        messageRepository.insertChatMessage(autoReplyMessage)
                        
                        // Tắt typing indicator sau khi gửi tin nhắn
                        val updatedState = _chatUiState.value.toMutableMap()
                        val updatedExistingState = updatedState[messageId]
                        updatedState[messageId] = updatedExistingState?.copy(isTyping = false)
                            ?: ChatUiState(isTyping = false)
                        _chatUiState.value = updatedState
                        
                        // Tăng reply index và lưu lại
                        val nextIndex = (currentIndex + 1) % when (contactName) {
                            "Mẹ" -> AutoReplyHelper.getTotalReplies()
                            "Người yêu" -> AutoReplyHelper.getTotalLoverReplies()
                            "Bác sĩ" -> AutoReplyHelper.getTotalDoctorReplies()
                            "Nhà khoa học" -> AutoReplyHelper.getTotalScientistReplies()
                            else -> 1
                        }
                        prefs?.edit()?.putInt("reply_index_$messageId", nextIndex)?.apply()
                    }
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
    
    fun sendMessageFromContact(messageId: String, text: String, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                // Gửi tin nhắn từ contact (người khác)
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    messageId = messageId,
                    text = text,
                    timestamp = Date(),
                    isFromMe = false,
                    imageUri = imageUri,
                    replyToMessageId = null
                )
                messageRepository.insertChatMessage(chatMessage)
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[messageId]
                currentState[messageId] = existingState?.copy(errorMessage = e.message)
                    ?: ChatUiState(errorMessage = e.message)
                _chatUiState.value = currentState
            }
        }
    }
    
    fun deleteChatMessage(chatMessageId: String, messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.deleteChatMessage(chatMessageId)
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[messageId]
                currentState[messageId] = existingState?.copy(errorMessage = e.message)
                    ?: ChatUiState(errorMessage = e.message)
                _chatUiState.value = currentState
            }
        }
    }
    
    fun updateChatMessage(chatMessage: ChatMessage) {
        viewModelScope.launch {
            try {
                messageRepository.updateChatMessage(chatMessage)
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[chatMessage.messageId]
                currentState[chatMessage.messageId] = existingState?.copy(errorMessage = e.message)
                    ?: ChatUiState(errorMessage = e.message)
                _chatUiState.value = currentState
            }
        }
    }
    
    fun replyToChatMessage(messageId: String, replyText: String, originalMessage: ChatMessage) {
        viewModelScope.launch {
            try {
                // Tạo tin nhắn phản hồi với replyToMessageId để biết đang phản hồi tin nhắn nào
                val replyMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    messageId = messageId,
                    text = replyText,
                    timestamp = Date(),
                    isFromMe = true,
                    imageUri = null,
                    replyToMessageId = originalMessage.id // Lưu ID của tin nhắn gốc
                )
                messageRepository.insertChatMessage(replyMessage)
            } catch (e: Exception) {
                val currentState = _chatUiState.value.toMutableMap()
                val existingState = currentState[messageId]
                currentState[messageId] = existingState?.copy(errorMessage = e.message)
                    ?: ChatUiState(errorMessage = e.message)
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

