package com.example.thebusysimulator.presentation.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.thebusysimulator.R
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.thebusysimulator.domain.model.Message
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.util.ImageHelper
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@Composable
fun MessageScreen(
    navController: NavController,
    viewModel: MessageViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages = uiState.messages
    val context = LocalContext.current

    // State quản lý Dialog tạo mới
    var showAddDialog by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.background, colorScheme.surface)
                )
            )
            .hideKeyboardOnClick()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarPadding()
        ) {
            // --- CUSTOM APP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tin nhắn",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Nút Add nổi bật hơn
                FilledTonalIconButton(
                    onClick = { showAddDialog = true },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Message"
                    )
                }
            }

            // --- MESSAGE LIST ---
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Icon Empty State
                        Icon(
                            imageVector = Icons.Rounded.Add, // Hoặc icon ChatBubble nếu có
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .background(colorScheme.surfaceVariant, CircleShape)
                                .padding(20.dp),
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Chưa có tin nhắn nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bấm nút + ở góc trên để tạo một tin nhắn giả từ người nổi tiếng hoặc crush!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageItem(
                            message = message,
                            onClick = {
                                navController.navigate(
                                    Screen.Chat.createRoute(
                                        contactName = message.contactName,
                                        messageId = message.id
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // --- CUSTOM DIALOG "XỊN" ---
    if (showAddDialog) {
        CreateMessageDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, uri, isVerified ->
                viewModel.addMessage(name, uri, isVerified)
                showAddDialog = false
            }
        )
    }
}

// Composable riêng cho Dialog để code gọn gàng
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateMessageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var newContactName by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isVerified by remember { mutableStateOf(false) }

    // Launcher chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedAvatarUri = uri
    }

    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp) // Margin bên ngoài
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Người gửi mới",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- AVATAR PICKER ---
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // Avatar chính - Click để chọn ảnh
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable { 
                                imagePickerLauncher.launch("image/*")
                            }
                            .then(
                                if (selectedAvatarUri == null) {
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(colorScheme.primary, colorScheme.tertiary)
                                        ),
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedAvatarUri),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Chữ cái đầu mặc định
                            Text(
                                text = if(newContactName.isNotBlank()) newContactName.take(1).uppercase() else "?",
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Nút Verify Badge - Chỉ hiện khi bật verified
                    if (isVerified) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset(x = 4.dp, y = 4.dp)
                                .background(colorScheme.surface, CircleShape)
                                .padding(3.dp) // Viền trắng
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_verify),
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- INPUT NAME ---
                OutlinedTextField(
                    value = newContactName,
                    onValueChange = { newContactName = it },
                    label = { Text("Tên hiển thị (VD: Elon Musk)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline
                    ),
                    leadingIcon = {
                        Icon(Icons.Rounded.Edit, null, tint = colorScheme.primary)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- VERIFIED TOGGLE ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_verify),
                            contentDescription = "Verified",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Người nổi tiếng (Verified)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- ACTION BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newContactName.isNotBlank()) {
                                scope.launch {
                                    // Lưu ảnh vào bộ nhớ trong để dùng lâu dài
                                    val savedPath = selectedAvatarUri?.let {
                                        ImageHelper.saveImageToInternalStorage(context, it)
                                    }
                                    onConfirm(newContactName, savedPath, isVerified)
                                }
                            }
                        },
                        enabled = newContactName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Tạo ngay", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val now = Date()
    val isToday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(message.timestamp) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)

    val timeText = if (isToday) {
        timeFormat.format(message.timestamp)
    } else {
        dateFormat.format(message.timestamp)
    }
    val context = LocalContext.current

    // Card tin nhắn được chuốt lại cho hiện đại
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp), // Bo góc nhiều hơn
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f) // Màu nền nhẹ hơn
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Phẳng
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .then(
                        if (message.avatarUri == null) {
                            Modifier.background(
                                brush = Brush.linearGradient(
                                    colors = listOf(colorScheme.primary, colorScheme.secondary)
                                ),
                                shape = CircleShape
                            )
                        } else {
                            Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (message.avatarUri != null) {
                    val imageData = remember(message.avatarUri) {
                        try {
                            if (message.avatarUri.startsWith("/")) {
                                val file = java.io.File(message.avatarUri)
                                if (file.exists()) file else null
                            } else {
                                message.avatarUri.toUri()
                            }
                        } catch (e: Exception) { null }
                    }

                    if (imageData != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(imageData)
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = message.contactName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = message.contactName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = message.contactName,
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (message.isVerified) {
                            Icon(
                                painter = painterResource(R.drawable.ic_verify),
                                contentDescription = "Verified",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.lastMessage.ifBlank { "Hình ảnh" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    fontWeight = if (isToday) FontWeight.Medium else FontWeight.Normal // Tin nhắn hôm nay thì đậm hơn chút
                )
            }
        }
    }
}