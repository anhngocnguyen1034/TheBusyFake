package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageViewModel
import com.example.thebusysimulator.presentation.viewmodel.ScheduledMessage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeMessageScreen(
    viewModel: FakeMessageViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Background Gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .statusBarPadding()
            .hideKeyboardOnClick()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp, top = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fake Message 💬",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Input Section
            item {
                MessageInputSection(viewModel = viewModel)
            }

            // Error Message
            if (uiState.errorMessage != null) {
                item {
                    ErrorBanner(
                        message = uiState.errorMessage ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            // Scheduled Messages List
            if (uiState.scheduledMessages.isNotEmpty()) {
                item {
                    Text(
                        text = "Tin nhắn đã lên lịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(uiState.scheduledMessages) { message ->
                    ScheduledMessageItem(
                        message = message,
                        onCancel = { viewModel.cancelMessage(message.id) }
                    )
                }
            } else {
                item {
                    EmptyMessageStateCard()
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputSection(
    viewModel: FakeMessageViewModel
) {
    var senderName by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    val quickTimeOptions = listOf(
        "Ngay lập tức" to 5,
        "1 phút" to 60,
        "5 phút" to 300,
        "30 phút" to 1800
    )

    var selectedDelaySeconds by remember { mutableStateOf(60) }
    var customTimeInput by remember { mutableStateOf("") }
    var selectedQuickOption by remember { mutableStateOf<String?>("1 phút") }

    fun createDateWithDelay(seconds: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, seconds)
        return calendar.time
    }

    fun formatDelay(seconds: Int): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60} phút ${(seconds % 60).let { if (it > 0) "$it s" else "" }}"
            else -> "${seconds / 3600} giờ"
        }
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Setup tin nhắn",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Input Sender Name & Message Text
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModernTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = "Ai sẽ nhắn cho bạn?",
                    icon = Icons.Rounded.Person
                )

                ModernTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = "Nội dung tin nhắn",
                    icon = Icons.Rounded.AccountBox,
                    keyboardType = KeyboardType.Text
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Time Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bao lâu nữa thì gửi?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickTimeOptions) { (label, seconds) ->
                        val isSelected = selectedQuickOption == label
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedQuickOption = label
                                selectedDelaySeconds = seconds
                                customTimeInput = ""
                            },
                            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                            shape = RoundedCornerShape(50),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Custom Input + Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customTimeInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                customTimeInput = newValue
                                selectedQuickOption = null
                                if (newValue.isNotBlank()) {
                                    selectedDelaySeconds = newValue.toIntOrNull() ?: 0
                                }
                            }
                        },
                        placeholder = { Text("Số giây...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    if (selectedDelaySeconds > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatDelay(selectedDelaySeconds),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show Now Button
                OutlinedButton(
                    onClick = {
                        if (senderName.isNotBlank() && messageText.isNotBlank()) {
                            viewModel.showMessageNow(senderName, messageText)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = senderName.isNotBlank() && messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gửi ngay", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Schedule Button
                Button(
                    onClick = {
                        if (senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0) {
                            viewModel.scheduleMessage(
                                senderName,
                                messageText,
                                createDateWithDelay(selectedDelaySeconds)
                            )
                            // Reset
                            senderName = ""
                            messageText = ""
                            selectedDelaySeconds = 60
                            customTimeInput = ""
                            selectedQuickOption = "1 phút"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .then(
                            if (senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0) {
                                Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0) "Lên lịch" else "Nhập đầy đủ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledMessageItem(
    message: ScheduledMessage,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeUntilMessage = message.scheduledTime.time - System.currentTimeMillis()
    val secondsUntil = (timeUntilMessage / 1000).toInt()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (secondsUntil > 0) Icons.Default.Email else Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (secondsUntil > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val statusText = if (secondsUntil > 0) "Gửi lúc ${dateFormat.format(message.scheduledTime)}" else "Đã quá hạn"
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cancel Button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hủy",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyMessageStateCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.DateRange,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có tin nhắn nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tạo tin nhắn giả ngay để trốn họp nào! 🏃💨",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

