package com.enlightenment.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enlightenment.data.local.converter.Converters
import com.enlightenment.data.local.dao.AuditLogDao
import com.enlightenment.data.local.dao.DailyProgressDao
import com.enlightenment.data.local.dao.StoryDao
import com.enlightenment.data.local.dao.UserProgressDao
import com.enlightenment.data.local.entity.AuditLogEntity
import com.enlightenment.data.local.entity.DailyProgressEntity
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.data.local.entity.UserProgressEntity

@Database(
    entities = [
        StoryEntity::class,
        UserProgressEntity::class,
        DailyProgressEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun auditLogDao(): AuditLogDao
    
    companion object {
        const val DATABASE_NAME = "enlightenment_database"
    }
}