package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.R
import com.example.thebusysimulator.domain.model.FakeNotification
import com.example.thebusysimulator.domain.repository.FakeNotificationRepository
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.GenZBlue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Màu sắc giống FakeMessageScreen
private val PopPurple = Color(0xFF8F00FF)
private val PopPink = Color(0xFFFF006E)
private val PopCyan = Color(0xFF00F0FF)

@Composable
fun NotificationHistoryScreen(
    navController: NavController,
    repository: FakeNotificationRepository
) {
    val colorScheme = MaterialTheme.colorScheme
    val theme = getGenZTheme()

    var notifications by remember { mutableStateOf<List<FakeNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadNotifications() {
        scope.launch {
            isLoading = true
            repository.getNotificationHistory().fold(
                onSuccess = { notificationList ->
                    notifications = notificationList
                    isLoading = false
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Không thể tải lịch sử"
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    fun deleteAllNotifications() {
        scope.launch {
            isLoading = true
            repository.deleteAllNotifications().fold(
                onSuccess = {
                    notifications = emptyList()
                    isLoading = false
                    showDeleteAllDialog = false
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Không thể xóa lịch sử"
                    isLoading = false
                    showDeleteAllDialog = false
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarPadding()
    ) {
        // Dotted background giống Fake Notifications
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
            contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = theme.text
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(

                    ) {
                        Text(
                            text = "FAKE NOTIFICATION",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = theme.text,
                            modifier = Modifier.weight(1f)
                        )
                        if (notifications.isNotEmpty() && !isLoading) {
                            TextButton(onClick = { showDeleteAllDialog = true }) {
                                Text(
                                    text = "XÓA TẨT CẢ",
                                    color = theme.text,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }


                }
            }

            // Loading
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GenZBlue)
                    }
                }
            }

            // Error
            if (errorMessage != null && !isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Empty state
            if (notifications.isEmpty() && !isLoading && errorMessage == null) {
                item {
                    EmptyNotificationHistoryCard(theme = theme)
                }
            }

            // List of notifications
            items(items = notifications, key = { it.id }) { notification ->
                HistoryNotificationItem(
                    notification = notification,
                    theme = theme
                )
            }
        }

        // Dialog xác nhận xóa tất cả
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = {
                    Text(
                        text = "Xóa tất cả lịch sử?",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn xóa tất cả lịch sử thông báo? Hành động này không thể hoàn tác.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { deleteAllNotifications() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error
                        )
                    ) {
                        Text("Xóa tất cả")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAllDialog = false }
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun HistoryNotificationItem(
    notification: FakeNotification,
    theme: GenZThemeColors
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Neo-brutalism style card giống Fake Notifications
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(theme.shadow, RoundedCornerShape(16.dp))
        )

        // Content
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, theme.border, RoundedCornerShape(16.dp))
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
                        text = notification.senderName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notification.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = theme.text
                        )
                        if (notification.isScheduled) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GenZBlue.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "ĐÃ LÊN LỊCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GenZBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = notification.messageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.text.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(notification.sentTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = theme.text.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notification",
                    tint = GenZBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationHistoryCard(theme: GenZThemeColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(theme.shadow, RoundedCornerShape(20.dp))
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(2.dp, theme.border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .background(theme.surface, CircleShape)
                        .padding(16.dp),
                    tint = GenZBlue.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chưa có lịch sử thông báo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = theme.text
                )
                Text(
                    text = "Các thông báo fake bạn gửi sẽ hiện ở đây.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.text.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

