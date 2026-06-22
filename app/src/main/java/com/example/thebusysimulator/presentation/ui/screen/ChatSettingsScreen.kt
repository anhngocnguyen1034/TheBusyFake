package com.example.thebusysimulator.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.navigation.Screen
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.component.GenZBackButton
import com.example.thebusysimulator.presentation.util.ImageHelper
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel

/**
 * Standalone settings screen for a chat conversation, opened from the "more" button
 * in the chat header (replaces the old dropdown menu).
 */
@Composable
fun ChatSettingsScreen(
    navController: NavController,
    viewModel: MessageViewModel,
    messageId: String,
    contactName: String,
    avatarUri: String?,
    isVerified: Boolean,
    currentThemeId: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val displayName = getContactDisplayName(contactName)

    var showThemePicker by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenZBackButton(onClick = { navController.popBackStack() })
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        // Contact header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary))
                    ),
                contentAlignment = Alignment.Center
            ) {
                val imageUri = remember(avatarUri) {
                    avatarUri?.let { ImageHelper.getImageUri(context, it) }
                }
                if (imageUri != null) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context).data(imageUri).build()
                        ),
                        contentDescription = stringResource(R.string.avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                if (isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_verify),
                        contentDescription = stringResource(R.string.verified),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        // Options
        SettingsRow(
            icon = Icons.Rounded.Star,
            label = "Change theme",
            tint = colorScheme.onSurface,
            onClick = { showThemePicker = true }
        )
        SettingsRow(
            icon = Icons.Filled.Search,
            label = "Search",
            tint = colorScheme.onSurface,
            onClick = {
                // Hand the search action back to the chat screen.
                navController.previousBackStackEntry?.savedStateHandle?.set("chat_action", "search")
                navController.popBackStack()
            }
        )
        SettingsRow(
            icon = Icons.Filled.Edit,
            label = "Edit contact",
            tint = colorScheme.onSurface,
            onClick = {
                navController.navigate(Screen.EditContact.createRoute(messageId))
            }
        )
        SettingsRow(
            icon = Icons.Filled.Delete,
            label = "Delete chat",
            tint = colorScheme.error,
            labelColor = colorScheme.error,
            onClick = { showDeleteChatDialog = true }
        )
    }

    if (showThemePicker) {
        ChatThemePickerSheet(
            currentThemeId = currentThemeId,
            onThemeSelected = { themeId ->
                viewModel.updateChatTheme(messageId, themeId)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false }
        )
    }

    if (showDeleteChatDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChatDialog = false },
            title = {
                Text(
                    text = "Delete chat?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Delete \"$displayName\" and all messages? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage(messageId)
                        showDeleteChatDialog = false
                        // Skip the chat screen and return to the message list.
                        navController.popBackStack(Screen.Message.route, inclusive = false)
                    }
                ) {
                    Text("Delete", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    labelColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor
        )
    }
}
