package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.thebusysimulator.presentation.util.PermissionHelper
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeMessageScreen(
    viewModel: FakeMessageViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    // Launcher cho notification permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Nếu từ chối notification permission, đánh dấu để hiện dialog lần sau
            viewModel.markNotificationPermissionDenied()
            viewModel.clearPermissionRequest()
        } else {
            // Có quyền notification rồi, clear tất cả request
            viewModel.clearPermissionRequest()
        }
    }

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
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate(com.example.thebusysimulator.presentation.navigation.Screen.NotificationHistory.route) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(com.example.thebusysimulator.R.drawable.ic_history),
                            contentDescription = "Lịch sử thông báo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Input Section
            item {
                MessageInputSection(viewModel = viewModel)
            }

            // Error Message
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { viewModel.clearError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Xử lý quyền Notification (BẮT BUỘC)
        if (uiState.needsNotificationPermission && activity != null) {
            if (!uiState.shouldShowNotificationPermissionDialog) {
                // Lần đầu: Launch permission request trực tiếp
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
        
        // Dialog yêu cầu quyền Notification (nếu đã từ chối trước đó)
        if (uiState.needsNotificationPermission && uiState.shouldShowNotificationPermissionDialog) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.markNotificationPermissionDenied()
                    viewModel.clearPermissionRequest()
                },
                title = {
                    Text(
                        text = "Cần quyền thông báo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Để hiển thị tin nhắn giả, ứng dụng cần quyền thông báo. " +
                                "Vui lòng mở Cài đặt và bật thông báo cho ứng dụng.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markNotificationPermissionDenied()
                            viewModel.clearPermissionRequest()
                            // Mở settings để cấp quyền thông báo
                            viewModel.openNotificationSettings()
                        }
                    ) {
                        Text("Mở Cài đặt")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            viewModel.markNotificationPermissionDenied()
                            viewModel.clearPermissionRequest()
                        }
                    ) {
                        Text("Để sau")
                    }
                }
            )
        }
        
        // Dialog yêu cầu quyền SCHEDULE_EXACT_ALARM (chỉ hiện nếu đã từ chối trước đó)
        if (uiState.needsScheduleExactAlarmPermission && uiState.shouldShowPermissionDialog) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.markPermissionDenied()
                    viewModel.clearPermissionRequest()
                },
                title = {
                    Text(
                        text = "Cần quyền lên lịch chính xác",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Để lên lịch tin nhắn giả, ứng dụng cần quyền lên lịch chính xác. " +
                                "Vui lòng mở Cài đặt và cấp quyền cho ứng dụng.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markPermissionDenied()
                            viewModel.clearPermissionRequest()
                            // Mở settings để cấp quyền SCHEDULE_EXACT_ALARM
                            viewModel.openScheduleExactAlarmSettings()
                        }
                    ) {
                        Text("Mở Cài đặt")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            viewModel.markPermissionDenied()
                            viewModel.clearPermissionRequest()
                        }
                    ) {
                        Text("Để sau")
                    }
                }
            )
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
    val uiState by viewModel.uiState.collectAsState()

    val quickTimeOptions = listOf(
        "Ngay lập tức" to 5,
        "1 phút" to 60,
        "5 phút" to 300,
        "30 phút" to 1800
    )

    var selectedDelaySeconds by remember { mutableStateOf(60) }
    var customTimeInput by remember { mutableStateOf("") }
    var selectedQuickOption by remember { mutableStateOf<String?>("1 phút") }
    
    // Reset form khi schedule thành công
    LaunchedEffect(uiState.messageScheduledSuccessfully) {
        if (uiState.messageScheduledSuccessfully) {
            senderName = ""
            messageText = ""
            selectedDelaySeconds = 60
            customTimeInput = ""
            selectedQuickOption = "1 phút"
            viewModel.clearSuccessFlag()
        }
    }

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
                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Ai sẽ nhắn cho bạn?") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Nội dung tin nhắn") },
                    leadingIcon = {
                        Icon(Icons.Rounded.AccountBox, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
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
                            // Không reset ngay, đợi thông báo thành công từ ViewModel
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

