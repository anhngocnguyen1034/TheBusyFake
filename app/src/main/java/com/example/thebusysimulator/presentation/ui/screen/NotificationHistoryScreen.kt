package com.example.thebusysimulator.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thebusysimulator.R
import com.example.thebusysimulator.domain.model.FakeNotification
import com.example.thebusysimulator.domain.repository.FakeNotificationRepository
import com.example.thebusysimulator.presentation.ui.statusBarPadding
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
    var notifications by remember { mutableStateOf<List<FakeNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorScheme.background, colorScheme.surface)
                )
            )
            .statusBarPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp, start = 16.dp, end = 16.dp),
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
                            tint = colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Lịch sử thông báo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            brush = Brush.linearGradient(
                                colors = listOf(PopPurple, PopPink)
                            )
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Loading
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorScheme.primary)
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
                    EmptyNotificationHistoryCard(colorScheme = colorScheme)
                }
            }

            // List of notifications
            items(items = notifications, key = { it.id }) { notification ->
                HistoryNotificationItem(
                    notification = notification,
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
fun HistoryNotificationItem(
    notification: FakeNotification,
    colorScheme: ColorScheme
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.senderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = notification.messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateFormat.format(notification.sentTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    if (notification.isScheduled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "Đã lên lịch",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Rounded.Email,
                contentDescription = "Message",
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyNotificationHistoryCard(colorScheme: ColorScheme) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                    .padding(16.dp),
                tint = colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có lịch sử thông báo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = "Các thông báo đã gửi sẽ hiển thị ở đây",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
