package com.example.thebusysimulator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.thebusysimulator.presentation.ui.screen.*
import com.anhnn.language.LanguageScreen
import com.example.thebusysimulator.presentation.viewmodel.FakeCallViewModel
import com.example.thebusysimulator.presentation.viewmodel.FakeMessageViewModel
import com.example.thebusysimulator.presentation.viewmodel.MessageViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(
    navController: NavHostController,
    fakeCallViewModel: FakeCallViewModel? = null,
    fakeMessageViewModel: FakeMessageViewModel? = null,
    messageViewModel: MessageViewModel? = null,
    hasOverlayPermission: Boolean = false,
    hasCameraPermission: Boolean = false,
    onRequestOverlayPermission: () -> Unit = {},
    onRequestCameraPermission: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            MainScreen(navController = navController)
        }
        
        composable(Screen.FakeCall.route) {
            fakeCallViewModel?.let { viewModel ->
                FakeCallScreen(
                    viewModel = viewModel,
                    navController = navController,
                    hasOverlayPermission = hasOverlayPermission,
                    hasCameraPermission = hasCameraPermission,
                    onRequestOverlayPermission = onRequestOverlayPermission,
                    onRequestCameraPermission = onRequestCameraPermission
                )
            }
        }
        
        composable(Screen.FakeMessage.route) {
            fakeMessageViewModel?.let { viewModel ->
                FakeMessageScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
        
        composable(Screen.Message.route) {
            messageViewModel?.let { viewModel ->
                MessageScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
        
        composable(Screen.Chat.route) { backStackEntry ->
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
            messageViewModel?.let { viewModel ->
                ChatScreen(
                    navController = navController,
                    viewModel = viewModel,
                    contactName = URLDecoder.decode(contactName, StandardCharsets.UTF_8.name()),
                    messageId = URLDecoder.decode(messageId, StandardCharsets.UTF_8.name())
                )
            }
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                fakeMessageViewModel = fakeMessageViewModel
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(Screen.Language.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            LanguageScreen(
                onBack = { navController.popBackStack() },
                onLanguageSaved = { (context as? android.app.Activity)?.recreate() }
            )
        }
        
        composable(Screen.CallHistory.route) {
            CallHistoryScreen(
                navController = navController,
                repository = com.example.thebusysimulator.presentation.di.AppContainer.fakeCallRepository
            )
        }
        
        composable(Screen.NotificationHistory.route) {
            NotificationHistoryScreen(
                navController = navController,
                repository = com.example.thebusysimulator.presentation.di.AppContainer.fakeNotificationRepository
            )
        }
        
        composable(Screen.CreateMessage.route) {
            messageViewModel?.let { viewModel ->
                CreateMessageScreen(
                    navController = navController,
                    onConfirm = { name, uri, isVerified ->
                        viewModel.addMessage(name, uri, isVerified)
                    }
                )
            }
        }

        composable(
            route = Screen.EditContact.route,
            arguments = listOf(navArgument("messageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: return@composable
            messageViewModel?.let { viewModel ->
                val uiState by viewModel.uiState.collectAsState()
                val message = uiState.messages.find { it.id == messageId }
                if (message != null) {
                    CreateMessageScreen(
                        navController = navController,
                        onConfirm = { name, uri, isVerified ->
                            viewModel.updateContact(messageId, name, uri, isVerified)
                        },
                        initialName = message.contactName,
                        initialAvatarUri = message.avatarUri,
                        initialIsVerified = message.isVerified,
                        isEditMode = true
                    )
                }
            }
        }

        composable(
            route = Screen.ChatSettings.route,
            arguments = listOf(navArgument("messageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedMessageId = backStackEntry.arguments?.getString("messageId") ?: return@composable
            val messageId = URLDecoder.decode(encodedMessageId, StandardCharsets.UTF_8.name())
            messageViewModel?.let { viewModel ->
                val uiState by viewModel.uiState.collectAsState()
                val message = uiState.messages.find { it.id == messageId }
                if (message != null) {
                    ChatSettingsScreen(
                        navController = navController,
                        viewModel = viewModel,
                        messageId = messageId,
                        contactName = message.contactName,
                        avatarUri = message.avatarUri,
                        isVerified = message.isVerified,
                        currentThemeId = message.chatTheme
                    )
                }
            }
        }

        composable(
            route = Screen.ImageEditor.route,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType },
                navArgument("encodedPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: return@composable
            val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: return@composable
            val imagePath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name())
            messageViewModel?.let { viewModel ->
                ImageEditorScreen(
                    navController = navController,
                    messageId = messageId,
                    imagePath = imagePath,
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Policy.route) {
            PolicyScreen(navController = navController)
        }
    }
}

