package com.example.thebusysimulator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.thebusysimulator.data.dao.ChatMessageDao
import com.example.thebusysimulator.data.dao.FakeCallDao
import com.example.thebusysimulator.data.dao.FakeNotificationDao
import com.example.thebusysimulator.data.dao.MessageDao
import com.example.thebusysimulator.data.model.ChatMessageEntity
import com.example.thebusysimulator.data.model.FakeCallEntity
import com.example.thebusysimulator.data.model.FakeNotificationEntity
import com.example.thebusysimulator.data.model.MessageEntity

@Database(
    entities = [MessageEntity::class, ChatMessageEntity::class, FakeCallEntity::class, FakeNotificationEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun fakeCallDao(): FakeCallDao
    abstract fun fakeNotificationDao(): FakeNotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE messages ADD COLUMN avatarUri TEXT")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table without contactNumber
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS messages_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        contactName TEXT NOT NULL,
                        lastMessage TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        unreadCount INTEGER NOT NULL,
                        avatarUri TEXT
                    )
                """.trimIndent())
                
                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO messages_new (id, contactName, lastMessage, timestamp, unreadCount, avatarUri)
                    SELECT id, contactName, lastMessage, timestamp, unreadCount, avatarUri
                    FROM messages
                """.trimIndent())
                
                // Drop old table
                database.execSQL("DROP TABLE messages")
                
                // Rename new table
                database.execSQL("ALTER TABLE messages_new RENAME TO messages")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Thêm cột imageUri vào bảng chat_messages
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN imageUri TEXT")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Thêm cột replyToMessageId vào bảng chat_messages
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN replyToMessageId TEXT")
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Thêm cột isVerified vào bảng messages với default value là 0 (false)
                database.execSQL("ALTER TABLE messages ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tạo bảng fake_calls
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS fake_calls (
                        id TEXT NOT NULL PRIMARY KEY,
                        callerName TEXT NOT NULL,
                        callerNumber TEXT NOT NULL,
                        scheduledTime INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isVideoCall INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tạo bảng fake_notifications
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS fake_notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        senderName TEXT NOT NULL,
                        messageText TEXT NOT NULL,
                        sentTime INTEGER NOT NULL,
                        isScheduled INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                builder.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}

