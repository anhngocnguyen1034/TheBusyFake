package com.example.thebusysimulator.domain.repository

import com.example.thebusysimulator.domain.model.FakeNotification

/**
 * Repository interface for managing fake notifications
 */
interface FakeNotificationRepository {
    suspend fun saveNotification(notification: FakeNotification): Result<FakeNotification>
    suspend fun getNotificationHistory(): Result<List<FakeNotification>>
    suspend fun getNotificationById(notificationId: String): Result<FakeNotification>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(): Result<Unit>
}
