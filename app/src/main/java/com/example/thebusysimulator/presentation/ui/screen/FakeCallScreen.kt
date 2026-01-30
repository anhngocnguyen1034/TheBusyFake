package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
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
import com.example.thebusysimulator.presentation.ui.theme.DarkBackground
import com.example.thebusysimulator.presentation.ui.theme.DarkBorder
import com.example.thebusysimulator.presentation.ui.theme.DarkSurface
import com.example.thebusysimulator.presentation.ui.theme.DarkText
import com.example.thebusysimulator.presentation.ui.theme.GenZBlue
import com.example.thebusysimulator.presentation.ui.theme.GenZGreen
import com.example.thebusysimulator.presentation.ui.theme.GenZPink
import com.example.thebusysimulator.presentation.ui.theme.GenZYellow
import com.example.thebusysimulator.presentation.ui.theme.LightBackground
import com.example.thebusysimulator.presentation.ui.theme.LightBorder
import com.example.thebusysimulator.presentation.ui.theme.LightSurface
import com.example.thebusysimulator.presentation.ui.theme.LightText
import com.example.thebusysimulator.presentation.viewmodel.FakeCallViewModel
import java.util.*


@Composable
fun getLocalTheme(): GenZThemeColors {
    val isDark = isSystemInDarkTheme()
    return if (isDark) {
        GenZThemeColors(
            background = DarkBackground,
            surface = DarkSurface,
            border = DarkBorder,
            text = DarkText,
            shadow = Color.White, // Shadow trắng nổi bật trên nền đen
            pattern = Color.White.copy(alpha = 0.1f)
        )
    } else {
        GenZThemeColors(
            background = LightBackground,
            surface = LightSurface,
            border = LightBorder,
            text = LightText,
            shadow = Color.Black,
            pattern = Color.Black.copy(alpha = 0.1f)
        )
    }
}

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
    val theme = getLocalTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarPadding()
            .hideKeyboardOnClick()
    ) {
        // 1. Background Pattern (Chấm bi)
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
                start = 20.dp, // Padding rộng hơn chút cho style này
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp), // Khoảng cách thưa hơn
            modifier = Modifier.fillMaxSize()
        ) {
            // --- HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button (Vuông vức)
                    GenZIconButton(
                        icon = Icons.Rounded.ArrowBack,
                        theme = theme,
                        onClick = { navController.popBackStack() }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "FAKE CALL",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-1).sp
                        ),
                        color = theme.text,
                        modifier = Modifier.weight(1f)
                    )

                    // History Button
                    GenZIconButton(
                        iconRes = R.drawable.ic_history,
                        theme = theme,
                        onClick = { navController.navigate(Screen.CallHistory.route) },
                        backgroundColor = GenZBlue // Điểm nhấn màu xanh
                    )
                }
            }

            // --- INPUT SECTION ---
            item {
                InputSection(
                    uiState = uiState,
                    viewModel = viewModel,
                    theme = theme
                )
            }

            // --- SETTINGS SECTION ---
            item {
                SettingsSection(viewModel = viewModel, theme = theme)
            }
        }
    }
}

// --- COMPONENTS STYLE NEO-BRUTALISM ---

@Composable
fun GenZIconButton(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    theme: GenZThemeColors,
    onClick: () -> Unit,
    backgroundColor: Color = theme.surface
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offset by animateDpAsState(targetValue = if (isPressed) 0.dp else 4.dp)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(theme.shadow, RoundedCornerShape(8.dp))
        )
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offset, y = offset)
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(2.dp, theme.border, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(icon, null, tint = theme.text, modifier = Modifier.size(24.dp))
            } else if (iconRes != null) {
                Icon(painterResource(iconRes), null, tint = theme.text, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun GenZContainer(
    title: String,
    theme: GenZThemeColors,
    accentColor: Color = GenZYellow,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        // Label Tag trên đầu
        Box(
            modifier = Modifier
                .background(theme.text, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = theme.background // Text màu nền (đảo ngược)
            )
        }

        // Main Box
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize() // Shadow full size
                    .offset(x = 6.dp, y = 6.dp)
                    .background(theme.shadow, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
            )
            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surface, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
                    .border(2.dp, theme.border, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenZTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    theme: GenZThemeColors,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = theme.text
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = theme.text,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(theme.surface)
                        .border(2.dp, theme.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = theme.text,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Nhập vào đây...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = theme.text.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun InputSection(
    uiState: Any,
    viewModel: FakeCallViewModel,
    theme: GenZThemeColors
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

    GenZContainer(
        title = "THÔNG TIN NGƯỜI GỌI",
        theme = theme
    ) {
        GenZTextField(
            value = callerName,
            onValueChange = { callerName = it },
            label = "Ai gọi thế?",
            icon = Icons.Rounded.Person,
            theme = theme
        )

        GenZTextField(
            value = callerNumber,
            onValueChange = { callerNumber = it },
            label = "Số điện thoại",
            icon = Icons.Rounded.Phone,
            theme = theme,
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- TIME SELECTION ---
        Text(
            text = "BAO LÂU NỮA THÌ GỌI?",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = theme.text
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickTimeOptions) { (label, seconds) ->
                val isSelected = selectedQuickOption == label

                // Gen Z Chip
                Box(
                    modifier = Modifier
                        .clickable {
                            selectedQuickOption = label
                            selectedDelaySeconds = seconds
                            customTimeInput = ""
                        }
                        .background(
                            if (isSelected) GenZYellow else theme.surface,
                            RoundedCornerShape(8.dp)
                        )
                        .border(2.dp, theme.border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        color = theme.text,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Custom Time
        GenZTextField(
            value = customTimeInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    customTimeInput = newValue
                    selectedQuickOption = null
                    if (newValue.isNotBlank()) selectedDelaySeconds = newValue.toIntOrNull() ?: 0
                }
            },
            label = "Hoặc nhập số giây",
            icon = Icons.Rounded.Edit,
            theme = theme,
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(8.dp))

        // BIG ACTION BUTTON
        val isEnabled = callerName.isNotBlank() && selectedDelaySeconds > 0

        GenZButton(
            text = if (isEnabled) "LÊN LỊCH NGAY \uD83D\uDE80" else "NHẬP ĐỦ ĐI BẠN ƠI",
            theme = theme,
            color = if (isEnabled) GenZGreen else theme.surface,
            enabled = isEnabled,
            onClick = {
                if (isEnabled) {
                    viewModel.scheduleCall(
                        callerName,
                        callerNumber,
                        createDateWithDelay(selectedDelaySeconds)
                    )
                    // Reset Logic
                    callerName = ""
                    callerNumber = ""
                    selectedDelaySeconds = 5
                    customTimeInput = ""
                    selectedQuickOption = "Ngay lập tức"
                }
            }
        )
    }
}

@Composable
fun SettingsSection(viewModel: FakeCallViewModel, theme: GenZThemeColors) {
    val uiState by viewModel.uiState.collectAsState()

    GenZContainer(
        title = "TÙY CHỈNH NÂNG CAO",
        theme = theme
    ) {
        // Row Rung
        GenZSwitchRow(
            label = "Rung khi gọi",
            icon = Icons.Default.Phone,
            checked = uiState.vibrationEnabled,
            onCheckedChange = { viewModel.setVibrationEnabled(it) },
            theme = theme,
            accentColor = GenZPink
        )

        // Row Flash
        GenZSwitchRow(
            label = "Nháy đèn Flash",
            icon = Icons.Default.Star,
            checked = uiState.flashEnabled,
            onCheckedChange = { viewModel.setFlashEnabled(it) },
            theme = theme,
            accentColor = GenZBlue
        )
    }
}

@Composable
fun GenZSwitchRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    theme: GenZThemeColors,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, theme.border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor, RoundedCornerShape(6.dp))
                    .border(2.dp, theme.border, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = theme.text
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.surface,
                checkedTrackColor = theme.text,
                checkedBorderColor = theme.border,
                uncheckedThumbColor = theme.text,
                uncheckedTrackColor = theme.surface,
                uncheckedBorderColor = theme.border
            )
        )
    }
}

@Composable
fun GenZButton(
    text: String,
    theme: GenZThemeColors,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Nút xẹp xuống khi ấn
    val offset by animateDpAsState(targetValue = if (isPressed || !enabled) 0.dp else 6.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Shadow (chỉ hiện khi enabled)
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(theme.shadow, RoundedCornerShape(12.dp))
            )
        }

        // Button Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offset, y = offset)
                .background(if (enabled) color else theme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(2.dp, theme.border, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (enabled) Color.Black else theme.text.copy(alpha = 0.5f)
            )
        }
    }
}