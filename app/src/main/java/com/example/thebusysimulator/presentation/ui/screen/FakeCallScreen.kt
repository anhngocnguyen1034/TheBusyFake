package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.domain.model.FakeCall
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.hideKeyboardOnClick
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.viewmodel.FakeCallViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import com.example.thebusysimulator.R

private val PopPurple = Color(0xFF8F00FF)
private val PopPink = Color(0xFFFF006E)
private val PopCyan = Color(0xFF00F0FF)
private val PopYellow = Color(0xFFFFD600)

// Nền: Sử dụng MaterialTheme colorScheme
@Composable
fun getPopBackgroundBrush(): Brush {
    val colorScheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(colors = listOf(colorScheme.background, colorScheme.surface))
}

// Màu chữ chính: Sử dụng MaterialTheme colorScheme
@Composable
fun getPopTextColor(): Color = MaterialTheme.colorScheme.onBackground

// Màu nền Card: Sử dụng MaterialTheme colorScheme
@Composable
fun getCardBackgroundColor(): Color = MaterialTheme.colorScheme.surface

// Màu chữ phụ (Label): Sử dụng MaterialTheme colorScheme
@Composable
fun getLabelColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

// Shadow color
@Composable
fun getNeonCardShadow(): Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getPopBackgroundBrush())
            .statusBarPadding()
            .hideKeyboardOnClick()
    ) {
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
                            tint = getPopTextColor()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Fake Call 🎭",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(
                                colors = listOf(PopPurple, PopPink)
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate(Screen.CallHistory.route) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = "Lịch sử cuộc gọi",
                            tint = getPopTextColor()
                        )
                    }
                }
            }

            // --- PERMISSION WARNING ---
            if (!hasCameraPermission) {
                item {
                    PermissionWarningCard(onClick = onRequestCameraPermission)
                }
            }

            // --- INPUT SECTION ---
            item {
                InputSection(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            item {
                SettingsSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FunGradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) Brush.horizontalGradient(colors = listOf(PopPink, PopPurple))
                else Brush.horizontalGradient(colors = listOf(getLabelColor(), getLabelColor()))
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                color = Color.White.copy(alpha = if (enabled) 1f else 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = getCardBackgroundColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = getLabelColor()) },
        leadingIcon = {
            Icon(icon, null, tint = PopPurple)
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, getLabelColor().copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = getCardBackgroundColor(),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = getPopTextColor(),
            unfocusedTextColor = getPopTextColor()
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@SuppressLint("DefaultLocale")
@Composable
fun InputSection(
    uiState: Any,
    viewModel: FakeCallViewModel
) {
    var callerName by rememberSaveable { mutableStateOf("") }
    var callerNumber by rememberSaveable { mutableStateOf("") }

    val quickTimeOptions = listOf(
        "Ngay lập tức" to 5,
        "1 phút" to 60,
        "5 phút" to 300,
        "30 phút" to 1800
    )

    var selectedDelaySeconds by rememberSaveable { mutableStateOf(5) }
    var customTimeInput by rememberSaveable { mutableStateOf("") }
    var selectedQuickOption by rememberSaveable { mutableStateOf<String?>("Ngay lập tức") }

    fun createDateWithDelay(seconds: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, seconds)
        return calendar.time
    }

    NeoCard {
        Text(
            text = "SETUP CUỘC GỌI",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = getLabelColor()
        )

        PopTextField(
            value = callerName,
            onValueChange = { callerName = it },
            label = "Ai sẽ gọi? (VD: Sếp, Crush...)",
            icon = Icons.Rounded.Person
        )

        PopTextField(
            value = callerNumber,
            onValueChange = { callerNumber = it },
            label = "Số điện thoại (Tùy chọn)",
            icon = Icons.Rounded.Phone,
            keyboardType = KeyboardType.Phone
        )

        HorizontalDivider(color = getLabelColor().copy(alpha = 0.3f))

        Text(
            text = "BAO LÂU NỮA?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = getLabelColor()
        )

        // Quick Chips Style mới (Pill colorful)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickTimeOptions) { (label, seconds) ->
                val isSelected = selectedQuickOption == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            selectedQuickOption = label
                            selectedDelaySeconds = seconds
                            customTimeInput = ""
                        }
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(PopPurple, PopPink))
                            else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else getPopTextColor(),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
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
                keyboardType = KeyboardType.Number
            )
        }

        // BIG ACTION BUTTON
        val isEnabled = callerName.isNotBlank() && selectedDelaySeconds > 0
        FunGradientButton(
            text = if (isEnabled) "Lên Lịch Ngay 🚀" else "Nhập thông tin",
            onClick = {
                if (isEnabled) {
                    viewModel.scheduleCall(
                        callerName,
                        callerNumber,
                        createDateWithDelay(selectedDelaySeconds)
                    )
                    callerName = ""
                    callerNumber = ""
                    selectedDelaySeconds = 5
                    customTimeInput = ""
                    selectedQuickOption = "Ngay lập tức"
                }
            },
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsSection(viewModel: FakeCallViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    NeoCard {
        Text(
            text = "TÙY CHỈNH",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = getLabelColor()
        )

        // Row Rung
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PopYellow.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, null, tint = Color(0xFFFFA000))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Rung khi gọi", fontWeight = FontWeight.Bold)
            }
            Switch(
                checked = uiState.vibrationEnabled,
                onCheckedChange = { viewModel.setVibrationEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PopPink,
                    checkedTrackColor = PopPink.copy(alpha = 0.2f)
                )
            )
        }

        // Row Flash
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PopCyan.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFF00B0FF))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đèn Flash", fontWeight = FontWeight.Bold)
            }
            Switch(
                checked = uiState.flashEnabled,
                onCheckedChange = { viewModel.setFlashEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PopCyan,
                    checkedTrackColor = PopCyan.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun ScheduledCallItem(call: FakeCall, onCancel: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeUntilCall = call.scheduledTime.time - System.currentTimeMillis()
    val secondsUntil = (timeUntilCall / 1000).toInt()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = getCardBackgroundColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PopPurple, PopCyan))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = call.callerName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getPopTextColor()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (secondsUntil > 0) Color.Green else Color.Red,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (secondsUntil > 0) "Đổ chuông lúc ${dateFormat.format(call.scheduledTime)}" else "Đã quá hạn",
                        style = MaterialTheme.typography.bodySmall,
                        color = getLabelColor()
                    )
                }
            }

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, null, tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

// Giữ lại EmptyStateCard và PermissionWarningCard nhưng style lại chút cho khớp
@Composable
fun EmptyStateCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .background(getCardBackgroundColor().copy(alpha = 0.5f), CircleShape)
                .padding(16.dp),
            tint = PopPurple.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có lịch nào!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = getLabelColor()
        )
        Text("Trốn họp ngay thôi 🏃💨", color = getLabelColor().copy(alpha = 0.6f))
    }
}

@Composable
fun PermissionWarningCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, null, tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Cần quyền Camera", fontWeight = FontWeight.Bold, color = Color.Red)
                Text("Để Video Call hoạt động", fontSize = 12.sp, color = Color.Red.copy(0.7f))
            }
        }
    }
}