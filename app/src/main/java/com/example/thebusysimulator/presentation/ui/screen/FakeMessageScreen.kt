package com.example.thebusysimulator.presentation.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.GenZBlue
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageViewModel
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageUiState
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
    val theme = getGenZTheme()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            viewModel.markNotificationPermissionDenied()
            viewModel.clearPermissionRequest()
        } else {
            viewModel.clearPermissionRequest()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarPadding()
            .hideKeyboardOnClick()
    ) {
        // Background Pattern (Dotted)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 20.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawCircle(
                        color = theme.pattern,
                        radius = 1.dp.toPx(),
                        center = Offset(x.toFloat(), y.toFloat())
                    )
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 100.dp,
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = theme.text
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "FAKE NOTIFICATION",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = theme.text,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate(Screen.NotificationHistory.route) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = "Lịch sử thông báo",
                            tint = theme.text
                        )
                    }
                }
            }

            // --- INPUT SECTION ---
            item {
                MessageInputSection(
                    uiState = uiState,
                    viewModel = viewModel,
                    theme = theme
                )
            }

            // Error Message
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Permission Logic
        if (uiState.needsNotificationPermission && activity != null) {
            if (!uiState.shouldShowNotificationPermissionDialog) {
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }

        if (uiState.needsNotificationPermission && uiState.shouldShowNotificationPermissionDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.markNotificationPermissionDenied()
                    viewModel.clearPermissionRequest()
                },
                title = { Text("Cần quyền thông báo", fontWeight = FontWeight.Bold) },
                text = { Text("Để hiển thị tin nhắn giả, ứng dụng cần quyền thông báo.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.markNotificationPermissionDenied()
                        viewModel.clearPermissionRequest()
                        viewModel.openNotificationSettings()
                    }) { Text("Mở Cài đặt") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.markNotificationPermissionDenied()
                        viewModel.clearPermissionRequest()
                    }) { Text("Để sau") }
                }
            )
        }

        if (uiState.needsScheduleExactAlarmPermission && uiState.shouldShowPermissionDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.markPermissionDenied()
                    viewModel.clearPermissionRequest()
                },
                title = { Text("Cần quyền lên lịch", fontWeight = FontWeight.Bold) },
                text = { Text("Để lên lịch chính xác, ứng dụng cần quyền Alarm.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.markPermissionDenied()
                        viewModel.clearPermissionRequest()
                        viewModel.openScheduleExactAlarmSettings()
                    }) { Text("Mở Cài đặt") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.markPermissionDenied()
                        viewModel.clearPermissionRequest()
                    }) { Text("Để sau") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    theme: GenZThemeColors,
    accentColor: Color = GenZBlue,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = theme.text.copy(alpha = 0.5f)) },
        leadingIcon = {
            Icon(icon, null, tint = accentColor)
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, theme.border, RoundedCornerShape(8.dp)),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = theme.surface,
            focusedContainerColor = theme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = theme.text,
            unfocusedTextColor = theme.text
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun MessageInputSection(
    uiState: FakeMessageUiState,
    viewModel: FakeMessageViewModel,
    theme: GenZThemeColors
) {
    var senderName by rememberSaveable { mutableStateOf("") }
    var messageText by rememberSaveable { mutableStateOf("") }

    val quickTimeOptions = listOf(
        "Ngay lập tức" to 5,
        "1 phút" to 60,
        "5 phút" to 300,
        "30 phút" to 1800
    )

    var selectedDelaySeconds by rememberSaveable { mutableStateOf(5) }
    var customTimeInput by rememberSaveable { mutableStateOf("") }
    var selectedQuickOption by rememberSaveable { mutableStateOf<String?>("Ngay lập tức") }

    LaunchedEffect(uiState.messageScheduledSuccessfully) {
        if (uiState.messageScheduledSuccessfully) {
            senderName = ""
            messageText = ""
            selectedDelaySeconds = 5
            customTimeInput = ""
            selectedQuickOption = "Ngay lập tức"
            viewModel.clearSuccessFlag()
        }
    }

    fun createDateWithDelay(seconds: Int): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, seconds)
        return calendar.time
    }

    GenZContainer(
        title = "SETUP TIN NHẮN",
        theme = theme,
        accentColor = GenZBlue
    ) {

        PopTextField(
            value = senderName,
            onValueChange = { senderName = it },
            label = "Ai sẽ nhắn? (VD: Sếp, Crush...)",
            icon = Icons.Rounded.Person,
            theme = theme,
            accentColor = GenZBlue
        )

        PopTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = "Nội dung tin nhắn",
            icon = Icons.Rounded.Email,
            theme = theme,
            accentColor = GenZBlue
        )

        HorizontalDivider(color = theme.border.copy(alpha = 0.3f))

        Text(
            text = "BAO LÂU NỮA?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = theme.text
        )

        // Quick Chips Style Neo-Brutalism
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickTimeOptions) { (label, seconds) ->
                val isSelected = selectedQuickOption == label
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val offsetState by animateDpAsState(
                    targetValue = if (isPressed) 0.dp else 2.dp,
                    label = "chipOffset"
                )

                Box(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            selectedQuickOption = label
                            selectedDelaySeconds = seconds
                            customTimeInput = ""
                        }
                ) {
                    // Shadow
                    Box(
                        modifier = Modifier
                            .offset(x = 2.dp, y = 2.dp)
                            .background(theme.shadow, RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                    // Content
                    Box(
                        modifier = Modifier
                            .offset(x = offsetState, y = offsetState)
                            .background(
                                if (isSelected) GenZBlue else theme.surface,
                                RoundedCornerShape(50)
                            )
                            .border(
                                2.dp,
                                theme.border,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else theme.text,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Custom Time Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            PopTextField(
                value = customTimeInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        customTimeInput = newValue
                        selectedQuickOption = null
                        if (newValue.isNotBlank()) selectedDelaySeconds =
                            newValue.toIntOrNull() ?: 0
                    }
                },
                label = "Số giây tùy chỉnh...",
                icon = Icons.Rounded.Edit,
                theme = theme,
                accentColor = GenZBlue,
                keyboardType = KeyboardType.Number
            )
        }

        // BIG ACTION BUTTON (Neo-Brutalism style)
        val isEnabled = senderName.isNotBlank() && messageText.isNotBlank() && selectedDelaySeconds > 0
        val buttonInteractionSource = remember { MutableInteractionSource() }
        val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
        val buttonOffset by animateDpAsState(
            targetValue = if (isButtonPressed) 0.dp else 4.dp,
            label = "buttonOffset"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(bottom = 4.dp)
                .clickable(
                    interactionSource = buttonInteractionSource,
                    indication = null,
                    enabled = isEnabled,
                    onClick = {
                        if (isEnabled) {
                            if (selectedDelaySeconds <= 10) {
                                // Gửi ngay
                                viewModel.showMessageNow(senderName, messageText)
                            } else {
                                // Lên lịch
                                viewModel.scheduleMessage(
                                    senderName,
                                    messageText,
                                    createDateWithDelay(selectedDelaySeconds)
                                )
                            }
                            senderName = ""
                            messageText = ""
                            selectedDelaySeconds = 5
                            customTimeInput = ""
                            selectedQuickOption = "Ngay lập tức"
                        }
                    }
                )
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(theme.shadow, RoundedCornerShape(12.dp))
            )
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = buttonOffset, y = buttonOffset)
                    .background(
                        if (isEnabled) GenZBlue else theme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(2.dp, theme.border, RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEnabled && selectedDelaySeconds <= 10) {
                        Icon(
                            Icons.Rounded.Send,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (isEnabled) {
                        Icon(
                            Icons.Rounded.Notifications,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isEnabled) {
                            if (selectedDelaySeconds <= 10) "GỬI NGAY 🚀" else "LÊN LỊCH GỬI"
                        } else "NHẬP THÔNG TIN",
                        color = if (isEnabled) Color.Black else theme.text.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
