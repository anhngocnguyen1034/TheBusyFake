package com.example.thebusysimulator.data.repository

import com.example.thebusysimulator.data.datasource.LocalFakeNotificationDataSource
import com.example.thebusysimulator.data.model.FakeNotificationData
import com.example.thebusysimulator.domain.model.FakeNotification
import com.example.thebusysimulator.domain.repository.FakeNotificationRepository
import java.util.Date

/**
 * Repository implementation for fake notifications
 */
class FakeNotificationRepositoryImpl(
    private val localDataSource: LocalFakeNotificationDataSource
) : FakeNotificationRepository {

    override suspend fun saveNotification(notification: FakeNotification): Result<FakeNotification> {
        return try {
            val notificationData = FakeNotificationData(
                id = notification.id,
                senderName = notification.senderName,
                messageText = notification.messageText,
                sentTime = notification.sentTime.time,
                isScheduled = notification.isScheduled
            )
            localDataSource.saveNotification(notificationData).getOrElse {
                return Result.failure(it)
            }
            Result.success(notification)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationHistory(): Result<List<FakeNotification>> {
        return try {
            val notificationsData = localDataSource.getAllNotifications().getOrElse {
                return Result.failure(it)
            }
            val notifications = notificationsData.map {
                FakeNotification(
                    id = it.id,
                    senderName = it.senderName,
                    messageText = it.messageText,
                    sentTime = Date(it.sentTime),
                    isScheduled = it.isScheduled
                )
            }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationById(notificationId: String): Result<FakeNotification> {
        return try {
            val notificationData = localDataSource.getNotificationById(notificationId).getOrElse {
                return Result.failure(it)
            }
            notificationData?.let {
                Result.success(
                    FakeNotification(
                        id = it.id,
                        senderName = it.senderName,
                        messageText = it.messageText,
                        sentTime = Date(it.sentTime),
                        isScheduled = it.isScheduled
                    )
                )
            } ?: Result.failure(IllegalArgumentException("Notification not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            localDataSource.deleteNotification(notificationId).getOrElse {
                return Result.failure(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
