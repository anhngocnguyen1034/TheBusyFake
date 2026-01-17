package com.example.thebusysimulator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.thebusysimulator.presentation.ui.screen.*
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
            LanguageSelectionScreen(navController = navController)
        }
    }
}

