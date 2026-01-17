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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
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
import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.viewmodel.FakeCallViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeCallScreen(
    viewModel: FakeCallViewModel,
    navController: NavController,
    hasOverlayPermission: Boolean,
    hasCameraPermission: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Background Gradient trẻ trung (Pastel Style)
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
            // --- HEADER: To, đậm, phong cách ---
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
                        text = "Fake Call 🎭",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // --- PERMISSION WARNINGS (Nếu có) ---
            if (!hasCameraPermission) {
                item {
                    PermissionWarningCard(onClick = onRequestCameraPermission)
                }
            }

            // --- INPUT CARD: Modern Glassy Effect ---
            item {
                InputSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // --- ERROR MESSAGE ---
            if (uiState.errorMessage != null) {
                item {
                    ErrorBanner(
                        message = uiState.errorMessage ?: "",
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            // --- SETTINGS SECTION: Rung và Flash ---
            item {
                SettingsSection(viewModel = viewModel)
            }
        }
    }
}

// --- SUB-COMPONENTS ĐÃ ĐƯỢC STYLE LẠI ---

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputSection(
    uiState: Any,
    viewModel: FakeCallViewModel
) {
    var callerName by remember { mutableStateOf("") }
    var callerNumber by remember { mutableStateOf("") }

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

    // Card Input được thiết kế nổi khối, bo góc lớn
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Setup cuộc gọi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Input Name & Number - Style mới gọn gàng hơn
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModernTextField(
                    value = callerName,
                    onValueChange = { callerName = it },
                    label = "Ai sẽ gọi cho bạn?",
                    icon = Icons.Rounded.Person
                )

                ModernTextField(
                    value = callerNumber,
                    onValueChange = { callerNumber = it },
                    label = "Số điện thoại (tùy chọn)",
                    icon = Icons.Rounded.Phone,
                    keyboardType = KeyboardType.Phone
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Time Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Bao lâu nữa thì gọi?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Chips dạng Pill
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
                            shape = RoundedCornerShape(50), // Bo tròn hoàn toàn
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

                    // Hiển thị tổng thời gian delay
                    if (selectedDelaySeconds > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle, // Icon giữ nguyên
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

            // Big Action Button
            val isEnabled = callerName.isNotBlank() && selectedDelaySeconds > 0
            Button(
                onClick = {
                    if (isEnabled) {
                        viewModel.scheduleCall(
                            callerName,
                            callerNumber,
                            createDateWithDelay(selectedDelaySeconds)
                        )
                        // Reset
                        callerName = ""
                        callerNumber = ""
                        selectedDelaySeconds = 60
                        customTimeInput = ""
                        selectedQuickOption = "1 phút"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(
                        if (isEnabled) {
                            Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isEnabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnabled) "Lên Lịch Ngay" else "Nhập tên và thời gian",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ScheduledCallItem(
    call: FakeCall,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeUntilCall = call.scheduledTime.time - System.currentTimeMillis()
    val secondsUntil = (timeUntilCall / 1000).toInt()

    // Thiết kế dạng thẻ thông báo (Notification Style)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .animateContentSize() // Animation mượt khi list thay đổi
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar sinh động
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
                    text = call.callerName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon giữ nguyên
                    Icon(
                        imageVector = if (secondsUntil > 0) Icons.Default.Menu else Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (secondsUntil > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val statusText = if (secondsUntil > 0) "Đổ chuông lúc ${dateFormat.format(call.scheduledTime)}" else "Đã quá hạn"
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Nút hủy dạng icon tròn
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

// --- UI HELPER COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    // TextField phong cách nền xám nhạt, bỏ border
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun PermissionWarningCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer) // Icon giữ nguyên
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Yêu cầu quyền Camera", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("Để kích hoạt tính năng Video Call", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Text("CẤP QUYỀN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun SettingsSection(viewModel: FakeCallViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val vibrationEnabled = uiState.vibrationEnabled
    val flashEnabled = uiState.flashEnabled

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
                text = "Cài đặt cuộc gọi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Switch Rung
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Rung khi gọi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Bật rung khi có cuộc gọi đến",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Switch Flash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nháy flash khi gọi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Bật đèn flash khi có cuộc gọi đến",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = flashEnabled,
                    onCheckedChange = { viewModel.setFlashEnabled(it) }
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon giữ nguyên
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có cuộc gọi nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Thêm lịch ngay để trốn họp nào! 🏃💨",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}