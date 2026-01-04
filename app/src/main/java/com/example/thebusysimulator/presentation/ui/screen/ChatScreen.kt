package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.navigation.NavController
import com.example.thebusysimulator.domain.model.ChatMessage
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: MessageViewModel,
    contactName: String,
    messageId: String
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // Load chat messages when screen opens
    LaunchedEffect(messageId) {
        viewModel.loadChatMessages(messageId)
    }
    
    val chatUiState by viewModel.chatUiState.collectAsState()
    val messages = chatUiState[messageId]?.chatMessages ?: emptyList()
    val isTyping = chatUiState[messageId]?.isTyping ?: false
    
    // Đảo ngược danh sách tin nhắn để tin nhắn mới nhất ở index 0 (dưới cùng với reverseLayout)
    val reversedMessages = remember(messages) {
        messages.reversed()
    }
    
    var previousMessageCount by remember(messageId) { mutableStateOf(messages.size) }
    var previousTypingState by remember(messageId) { mutableStateOf(isTyping) }
    
    // Tự động scroll khi có tin nhắn mới hoặc typing indicator xuất hiện
    LaunchedEffect(messages.size, isTyping) {
        val currentCount = messages.size
        val itemCount = reversedMessages.size + if (isTyping) 1 else 0
        
        // Khi có tin nhắn mới (số lượng tăng)
        if (currentCount > previousMessageCount && itemCount > 0) {
            kotlinx.coroutines.delay(50) // Đợi một chút để UI cập nhật
            // Với reverseLayout, scroll đến index 0 (tin nhắn mới nhất ở dưới cùng)
            listState.animateScrollToItem(0)
            previousMessageCount = currentCount
        } 
        // Khi typing indicator xuất hiện (chuyển từ false sang true)
        else if (isTyping && !previousTypingState && itemCount > 0) {
            kotlinx.coroutines.delay(50)
            listState.animateScrollToItem(0)
        }
        
        previousTypingState = isTyping
        if (currentCount > previousMessageCount) {
            previousMessageCount = currentCount
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.BackgroundStart, AppColors.BackgroundEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.BottomNavBg
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.Accent, AppColors.AccentSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contactName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contactName,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.BottomNavBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Video Call Button
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val intent = Intent(context, com.example.thebusysimulator.presentation.FakeVideoCallActivity::class.java).apply {
                                putExtra("caller_name", contactName)
                                putExtra("caller_number", "")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Video Call",
                            tint = AppColors.Accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            // Messages list với reverseLayout để tin nhắn mới nhất ở dưới cùng
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding(), // Để bàn phím đẩy nội dung lên mượt mà
                reverseLayout = true, // Bật reverseLayout: tin nhắn mới nhất ở index 0 (dưới cùng)
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hiển thị typing indicator khi đang chờ tin nhắn (ở đầu list vì reverseLayout)
                if (isTyping) {
                    item {
                        TypingIndicatorBubble()
                    }
                }
                
                // Tin nhắn đã được đảo ngược: tin nhắn mới nhất ở index 0
                items(reversedMessages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
            
            // Input field
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                text = "Type a message...",
                                color = AppColors.BottomNavBg.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.BottomNavBg,
                            unfocusedTextColor = AppColors.BottomNavBg,
                            focusedBorderColor = AppColors.Accent,
                            unfocusedBorderColor = AppColors.BottomNavBg.copy(alpha = 0.3f),
                            cursorColor = AppColors.Accent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendChatMessage(messageId, messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (messageText.isNotBlank()) {
                                    AppColors.Accent
                                } else {
                                    AppColors.BottomNavBg.copy(alpha = 0.3f)
                                }
                            ),
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) Color.White else AppColors.BottomNavBg.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (message.isFromMe) {
                            AppColors.Accent
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else AppColors.BottomNavBg,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = timeFormat.format(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.BottomNavBg.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    // Animation cho typing indicator
    val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "typing_animation"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TypingIndicator(animatedValue = animatedValue)
            }
        }
    }
}

@Composable
fun TypingIndicator(animatedValue: Float) {
    val dotColor = Color(0xFF909090)
    val dotRadius = 3.dp
    val spacing = 5.dp
    val jumpHeight = 3.dp

    Canvas(
        modifier = Modifier
            .size(width = dotRadius * 2 * 3 + spacing * 2, height = dotRadius * 2 + jumpHeight * 2)
    ) {
        val centerY = size.height / 2
        val startX = dotRadius.toPx()

        // Vẽ 3 chấm tròn với animation
        for (i in 0..2) {
            val phase = (animatedValue / 100f * 2 * kotlin.math.PI).toFloat() + (i * 2.5f)
            val yOffset = sin(phase) * jumpHeight.toPx()

            val x = startX + (i * (dotRadius.toPx() * 2 + spacing.toPx()))
            val y = centerY + yOffset

            drawCircle(
                color = dotColor,
                radius = dotRadius.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
