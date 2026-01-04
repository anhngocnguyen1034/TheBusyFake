package com.example.thebusysimulator.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object FakeCall : Screen("fake_call")
    data object Message : Screen("message")
    data object Chat : Screen("chat/{contactName}/{messageId}") {
        fun createRoute(contactName: String, messageId: String): String {
            val encodedName = URLEncoder.encode(contactName, StandardCharsets.UTF_8.name())
            val encodedMessageId = URLEncoder.encode(messageId, StandardCharsets.UTF_8.name())
            return "chat/$encodedName/$encodedMessageId"
        }
    }
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
}

