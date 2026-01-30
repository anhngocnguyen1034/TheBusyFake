package com.example.thebusysimulator.data.datasource

import com.example.thebusysimulator.data.model.FakeNotificationData

/**
 * Local data source for storing fake notifications
 */
interface LocalFakeNotificationDataSource {
    suspend fun saveNotification(notification: FakeNotificationData): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(): Result<Unit>
    suspend fun getNotificationById(notificationId: String): Result<FakeNotificationData?>
    suspend fun getAllNotifications(): Result<List<FakeNotificationData>>
}
