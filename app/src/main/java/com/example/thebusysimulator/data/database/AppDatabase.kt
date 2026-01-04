package com.example.thebusysimulator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.thebusysimulator.data.dao.ChatMessageDao
import com.example.thebusysimulator.data.dao.MessageDao
import com.example.thebusysimulator.data.model.ChatMessageEntity
import com.example.thebusysimulator.data.model.MessageEntity

@Database(
    entities = [MessageEntity::class, ChatMessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatMessageDao(): ChatMessageDao
    
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
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                builder.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}

