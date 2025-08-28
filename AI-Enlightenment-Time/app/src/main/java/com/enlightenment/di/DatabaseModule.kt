package com.enlightenment.di

import android.content.Context
import androidx.room.Room
import com.enlightenment.data.local.dao.StoryDao
import com.enlightenment.data.local.dao.UserProgressDao
import com.enlightenment.data.local.database.AppDatabase



object DatabaseModule {
    
    
    
    fun provideAppDatabase(
        context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }
    
    
    fun provideUserProgressDao(database: AppDatabase): UserProgressDao {
        return database.userProgressDao()
    }
}