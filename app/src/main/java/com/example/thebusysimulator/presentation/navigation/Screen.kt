package com.example.thebusysimulator.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object FakeCall : Screen("fake_call")
    data object FakeMessage : Screen("fake_message")
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
    data object Language : Screen("language")
    data object CallHistory : Screen("call_history")
    data object NotificationHistory : Screen("notification_history")
    data object CreateMessage : Screen("create_message")
    data object EditContact : Screen("edit_contact/{messageId}") {
        fun createRoute(messageId: String): String = "edit_contact/$messageId"
    }
    data object ChatSettings : Screen("chat_settings/{messageId}") {
        fun createRoute(messageId: String): String {
            val encodedMessageId = URLEncoder.encode(messageId, StandardCharsets.UTF_8.name())
            return "chat_settings/$encodedMessageId"
        }
    }
    data object ImageEditor : Screen("image_editor/{messageId}/{encodedPath}") {
        fun createRoute(messageId: String, imagePath: String): String {
            val encoded = URLEncoder.encode(imagePath, StandardCharsets.UTF_8.name())
            return "image_editor/$messageId/$encoded"
        }
    }
    data object Policy : Screen("policy")
}

