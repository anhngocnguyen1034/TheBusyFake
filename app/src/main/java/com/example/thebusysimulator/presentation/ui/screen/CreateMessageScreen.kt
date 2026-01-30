package com.example.thebusysimulator.presentation.ui.screen

import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.thebusysimulator.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.GenZPink
import com.example.thebusysimulator.presentation.ui.theme.GenZYellow
import com.example.thebusysimulator.presentation.util.ImageHelper
import kotlinx.coroutines.launch

@Composable
fun CreateMessageScreen(
    navController: NavController,
    onConfirm: (String, String?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme = getGenZTheme()
    
    var newContactName by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isVerified by remember { mutableStateOf(false) }

    // Launcher chọn ảnh
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedAvatarUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarPadding()
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = theme.text
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "TẠO NGƯỜI GỬI MỚI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = theme.text,
                    modifier = Modifier.weight(1f)
                )
            }

            // Main Content
            GenZContainer(
                title = "THÔNG TIN",
                theme = theme,
                accentColor = GenZPink
            ) {
                // Avatar Picker
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val offset by animateDpAsState(
                            targetValue = if (isPressed) 0.dp else 4.dp,
                            label = "avatarOffset"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 4.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    imagePickerLauncher.launch("image/*")
                                }
                        ) {
                            // Shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(theme.shadow, CircleShape)
                            )
                            // Content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = offset, y = offset)
                                    .clip(CircleShape)
                                    .then(
                                        if (selectedAvatarUri == null) {
                                            Modifier.background(GenZPink, CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                if (selectedAvatarUri != null) {
                                    androidx.compose.foundation.Image(
                                        painter = rememberAsyncImagePainter(selectedAvatarUri),
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    if (newContactName.isNotBlank()) {
                                        Text(
                                            text = newContactName.take(1).uppercase(),
                                            color = Color.Black,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.union__3_),
                                            contentDescription = "Chọn ảnh",
                                            tint = Color.Black,
                                            modifier = Modifier.size(48.dp).align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }

                        // Verified Badge
                        if (isVerified) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .offset(x = 4.dp, y = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .offset(x = 2.dp, y = 2.dp)
                                        .background(theme.shadow, CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(theme.surface, CircleShape)
                                        .border(2.dp, theme.border, CircleShape)
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(GenZYellow, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_verify),
                                            contentDescription = "Verified",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name Input
                    GenZTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = "Tên hiển thị (VD: Elon Musk)",
                        theme = theme,
                        accentColor = GenZPink
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verified Toggle
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
                                tint = GenZYellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "NGƯỜI NỔI TIẾNG",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = theme.text
                            )
                        }
                        Switch(
                            checked = isVerified,
                            onCheckedChange = { isVerified = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GenZYellow,
                                checkedTrackColor = GenZYellow.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Action Button
            val isEnabled = newContactName.isNotBlank()
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
                                scope.launch {
                                    val savedPath = selectedAvatarUri?.let {
                                        ImageHelper.saveImageToInternalStorage(context, it)
                                    }
                                    onConfirm(newContactName, savedPath, isVerified)
                                    navController.popBackStack()
                                }
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
                            if (isEnabled) GenZPink else theme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(2.dp, theme.border, RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEnabled) "TẠO NGAY 🚀" else "NHẬP TÊN",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenZTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    theme: GenZThemeColors,
    accentColor: Color = GenZYellow
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = theme.text.copy(alpha = 0.5f)) },
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
        )
    )
}
