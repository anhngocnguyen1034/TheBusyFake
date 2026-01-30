package com.example.thebusysimulator.data.datasource

import com.example.thebusysimulator.data.dao.FakeNotificationDao
import com.example.thebusysimulator.data.model.FakeNotificationData
import com.example.thebusysimulator.data.model.FakeNotificationEntity
import kotlinx.coroutines.flow.first

/**
 * Room database implementation of LocalFakeNotificationDataSource
 */
class LocalFakeNotificationDataSourceImpl(
    private val fakeNotificationDao: FakeNotificationDao
) : LocalFakeNotificationDataSource {

    override suspend fun saveNotification(notification: FakeNotificationData): Result<Unit> {
        return try {
            val entity = FakeNotificationEntity(
                id = notification.id,
                senderName = notification.senderName,
                messageText = notification.messageText,
                sentTime = notification.sentTime,
                isScheduled = notification.isScheduled
            )
            fakeNotificationDao.insertNotification(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            fakeNotificationDao.deleteNotificationById(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllNotifications(): Result<Unit> {
        return try {
            fakeNotificationDao.deleteAllNotifications()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationById(notificationId: String): Result<FakeNotificationData?> {
        return try {
            val entity = fakeNotificationDao.getNotificationById(notificationId)
            val data = entity?.let {
                FakeNotificationData(
                    id = it.id,
                    senderName = it.senderName,
                    messageText = it.messageText,
                    sentTime = it.sentTime,
                    isScheduled = it.isScheduled
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllNotifications(): Result<List<FakeNotificationData>> {
        return try {
            val entities = fakeNotificationDao.getAllNotifications().first()
            val data = entities.map {
                FakeNotificationData(
                    id = it.id,
                    senderName = it.senderName,
                    messageText = it.messageText,
                    sentTime = it.sentTime,
                    isScheduled = it.isScheduled
                )
            }
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
