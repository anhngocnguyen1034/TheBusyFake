package com.example.thebusysimulator.presentation.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.thebusysimulator.domain.model.ChatMessage
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.util.DateUtils
import com.example.thebusysimulator.presentation.util.ImageHelper
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: MessageViewModel,
    contactName: String,
    messageId: String
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- State quản lý Long Press & Reply ---
    var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    var selectedMessageIdForTimestamp by remember { mutableStateOf<String?>(null) }

    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    fun openMessageOptions(message: ChatMessage) {
        selectedMessageForMenu = message
        showBottomSheet = true
    }

    LaunchedEffect(messageId) {
        viewModel.loadChatMessages(messageId)
    }

    val chatUiState by viewModel.chatUiState.collectAsState()
    val messages = chatUiState[messageId]?.chatMessages ?: emptyList()
    val isTyping = chatUiState[messageId]?.isTyping ?: false
    val reversedMessages = remember(messages) { messages.reversed() }

    // Hàm để scroll đến tin nhắn được phản hồi
    fun scrollToMessage(replyToMessageId: String) {
        val index = reversedMessages.indexOfFirst { it.id == replyToMessageId }
        if (index != -1) {
            scope.launch {
                highlightedMessageId = replyToMessageId
                listState.animateScrollToItem(index)
                // Xóa highlight sau 2 giây
                kotlinx.coroutines.delay(2000)
                highlightedMessageId = null
            }
        }
    }

    // Auto scroll logic
    var previousMessageCount by remember(messageId) { mutableStateOf(messages.size) }
    var previousTypingState by remember(messageId) { mutableStateOf(isTyping) }

    LaunchedEffect(messages.size, isTyping) {
        val currentCount = messages.size
        val itemCount = reversedMessages.size + if (isTyping) 1 else 0
        if ((currentCount > previousMessageCount || (isTyping && !previousTypingState)) && itemCount > 0) {
            kotlinx.coroutines.delay(50)
            listState.animateScrollToItem(0)
        }
        previousTypingState = isTyping
        if (currentCount > previousMessageCount) previousMessageCount = currentCount
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(colorScheme.background, colorScheme.surface)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().statusBarPadding()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, "Back", tint = colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(
                            Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contactName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(contactName, style = MaterialTheme.typography.titleMedium, color = colorScheme.onBackground, modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        val intent = Intent(context, com.example.thebusysimulator.presentation.FakeVideoCallActivity::class.java).apply {
                            putExtra("caller_name", contactName)
                            putExtra("caller_number", "")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Call, "Video Call", tint = colorScheme.primary)
                    }
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        keyboardController?.hide()
                    },
                reverseLayout = true,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isTyping) {
                    item { TypingIndicatorBubble() }
                }
                itemsIndexed(reversedMessages, key = { _, message -> message.id }) { index, message ->
                    // Hiển thị header ở tin nhắn đầu tiên của mỗi ngày
                    // Vì list bị đảo ngược (tin mới nhất ở index 0), cần so sánh với tin nhắn cũ hơn (index + 1)
                    val shouldShowHeader = remember(index, message.timestamp, reversedMessages) {
                        val nextIndex = index + 1 // Tin nhắn cũ hơn liền kề
                        if (nextIndex < reversedMessages.size) {
                            val olderMessage = reversedMessages[nextIndex]
                            // Nếu ngày của tin hiện tại KHÁC ngày của tin cũ hơn -> Hiện Header
                            !DateUtils.isSameDate(message.timestamp, olderMessage.timestamp)
                        } else {
                            // Nếu đây là tin nhắn cuối cùng trong danh sách (tin già nhất) -> Luôn hiện Header
                            true
                        }
                    }
                    
                    Column {
                        if (shouldShowHeader) {
                            DateHeader(date = message.timestamp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        ChatBubble(
                            message = message,
                            allMessages = reversedMessages,
                            onLongClick = { openMessageOptions(message) },
                            onReplyClick = { replyId -> scrollToMessage(replyId) },
                            isHighlighted = message.id == highlightedMessageId,
                            showTimestamp = message.id == selectedMessageIdForTimestamp,
                            onClick = {
                                if (selectedMessageIdForTimestamp == message.id) {
                                    selectedMessageIdForTimestamp = null
                                } else {
                                    selectedMessageIdForTimestamp = message.id
                                }
                            }
                        )
                    }
                }
            }
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .windowInsetsPadding(WindowInsets.ime)
                ) {
                    AnimatedVisibility(
                        visible = replyingToMessage != null,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        replyingToMessage?.let { replyMsg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thanh dọc màu sắc
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Đang trả lời ${if(replyMsg.isFromMe) "chính mình" else contactName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = replyMsg.text.ifBlank { if (replyMsg.imageUri != null) "📷 [Hình ảnh]" else "" },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { replyingToMessage = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, "Cancel Reply", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val imagePickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickMultipleVisualMedia()
                        ) { uris: List<Uri> ->
                            if (uris.isNotEmpty()) {
                                scope.launch {
                                    uris.forEach { uri ->
                                        val imagePath = ImageHelper.saveChatImageToInternalStorage(context, uri)
                                        if (imagePath != null) {
                                            viewModel.sendChatMessage(messageId, "", imagePath)
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(onClick = {
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Rounded.AccountBox, "Select Image", tint = colorScheme.primary)
                        }
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f), // Màu nền xám nhẹ
                                    shape = RoundedCornerShape(24.dp) // Bo tròn như viên thuốc
                                ),
                            textStyle = TextStyle(
                                color = colorScheme.onBackground,
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            maxLines = 1,
                            cursorBrush = SolidColor(colorScheme.primary), // Màu con trỏ chuột
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (messageText.isEmpty()) {
                                            Text(
                                                "Nhập tin nhắn...",
                                                style = TextStyle(
                                                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                                                    fontSize = 16.sp
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    if (replyingToMessage != null) {
                                        viewModel.replyToChatMessage(messageId, messageText, replyingToMessage!!)
                                        replyingToMessage = null
                                    } else {
                                        viewModel.sendChatMessage(messageId, messageText, null)
                                    }
                                    messageText = ""
                                }
                            },
                           enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.Rounded.Send, "Send",
                                tint = if (messageText.isNotBlank()) colorScheme.primary else colorScheme.onBackground.copy(0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        replyingToMessage = selectedMessageForMenu
                        showBottomSheet = false
                    }.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = colorScheme.primary)
                    Text("Trả lời", style = MaterialTheme.typography.bodyLarge)
                }
                Divider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedMessageForMenu?.let { viewModel.deleteChatMessage(it.id, messageId) }
                        showBottomSheet = false
                    }.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Rounded.Delete, null, tint = colorScheme.error)
                    Text("Xóa tin nhắn", style = MaterialTheme.typography.bodyLarge, color = colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    allMessages: List<ChatMessage>,
    onLongClick: () -> Unit,
    onReplyClick: (String) -> Unit = {},
    isHighlighted: Boolean = false,
    showTimestamp: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val originalMessage = message.replyToMessageId?.let { replyId ->
        allMessages.find { it.id == replyId }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 2.dp),
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    .background(
                        if (isHighlighted) {
                            colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            if (message.isFromMe) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
            ) {
                Column {

                    originalMessage?.let { original ->
                        Row(
                            modifier = Modifier
                                .padding(start = 6.dp, end = 6.dp, top = 6.dp)
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    // Tạo màu nền tương phản nhẹ với màu bubble chính
                                    if (message.isFromMe) Color.Black.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.4f)
                                )
                                .clickable { 
                                    original.id.let { replyId -> onReplyClick(replyId) }
                                }
                        ) {
                            // 1. Thanh dọc (Vertical Bar)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(
                                        if (message.isFromMe) Color.White.copy(alpha = 0.7f)
                                        else colorScheme.primary
                                    )
                            )

                            // 2. Nội dung reply
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = if (original.isFromMe) "Bạn" else "Người khác", // Có thể thay bằng contactName nếu có
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (message.isFromMe) Color.White.copy(alpha = 0.9f) else colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        original.text.isNotBlank() -> original.text
                                        original.imageUri != null -> "📷 Hình ảnh"
                                        else -> "Tin nhắn đã xóa"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (message.isFromMe) Color.White.copy(alpha = 0.7f) else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    message.imageUri?.let { imageUri ->
                        val imageData = try {
                            if (imageUri.startsWith("/")) java.io.File(imageUri).takeIf { it.exists() } else Uri.parse(imageUri)
                        } catch (e: Exception) { null }

                        if (imageData != null) {
                            // Nếu có reply, thêm khoảng cách phía trên ảnh
                            if (originalMessage != null) Spacer(modifier = Modifier.height(8.dp))

                            Image(
                                painter = rememberAsyncImagePainter(ImageRequest.Builder(context).data(imageData).build()),
                                contentDescription = "Chat Image",
                                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = if (message.isFromMe) Color.White else colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else if (originalMessage != null && message.imageUri == null) {
                        // Trường hợp hiếm: chỉ reply mà không có text/ảnh (thường không xảy ra nhưng cứ padding cho đẹp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            AnimatedVisibility(
                visible = showTimestamp,
                enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
            ) {
                Text(
                    text = DateUtils.formatMessageTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

// Composable để hiển thị header ngày tháng
@Composable
fun DateHeader(date: java.util.Date) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DateUtils.getDateHeaderLabel(date),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .background(
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// Các hàm khác (TypingIndicatorBubble) giữ nguyên
@Composable
fun TypingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "typing_animation"
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            TypingIndicator(animatedValue = animatedValue)
        }
    }
}

@Composable
fun TypingIndicator(animatedValue: Float) {
    val dotColor = Color(0xFF909090); val dotRadius = 3.dp; val spacing = 5.dp; val jumpHeight = 3.dp
    Canvas(modifier = Modifier.size(width = dotRadius * 6 + spacing * 2, height = dotRadius * 2 + jumpHeight * 2)) {
        val centerY = size.height / 2; val startX = dotRadius.toPx()
        for (i in 0..2) {
            val phase = (animatedValue / 100f * 2 * kotlin.math.PI).toFloat() + (i * 2.5f)
            val yOffset = sin(phase) * jumpHeight.toPx()
            drawCircle(color = dotColor, radius = dotRadius.toPx(), center = Offset(startX + (i * (dotRadius.toPx() * 2 + spacing.toPx())), centerY + yOffset))
        }
    }
}