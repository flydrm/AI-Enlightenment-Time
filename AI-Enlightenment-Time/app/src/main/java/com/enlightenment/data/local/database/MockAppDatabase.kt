package com.enlightenment.data.local.database

import android.content.Context
import com.enlightenment.data.local.dao.*
import com.enlightenment.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * 模拟的数据库实现，用于解决编译问题
 */
class MockAppDatabase {
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = MockAppDatabaseImpl()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * 模拟的AppDatabase实现
 */
class MockAppDatabaseImpl : AppDatabase() {
    
    override fun storyDao(): StoryDao = MockStoryDao()
    override fun userProgressDao(): UserProgressDao = MockUserProgressDao()
    override fun dailyProgressDao(): DailyProgressDao = MockDailyProgressDao()
    override fun auditLogDao(): AuditLogDao = MockAuditLogDao()
    
    fun query(sql: String, args: Array<Any>?): Unit {
        // 模拟查询
    }
}

// 模拟的DAO实现
class MockStoryDao : StoryDao {
    private val stories = mutableListOf<StoryEntity>()
    
    override suspend fun insert(story: StoryEntity) {
        stories.add(story)
    }
    
    override suspend fun update(story: StoryEntity) {
        stories.removeIf { it.id == story.id }
        stories.add(story)
    }
    
    override suspend fun delete(story: StoryEntity) {
        stories.removeIf { it.id == story.id }
    }
    
    override suspend fun getStoryById(storyId: String): StoryEntity? {
        return stories.find { it.id == storyId }
    }
    
    override fun getAllStories(): Flow<List<StoryEntity>> {
        return flowOf(stories.toList())
    }
    
    override fun getStoriesByCategory(category: String): Flow<List<StoryEntity>> {
        return flowOf(stories.filter { it.category == category })
    }
    
    override fun getFavoriteStories(): Flow<List<StoryEntity>> {
        return flowOf(stories.filter { it.isFavorite })
    }
    
    override suspend fun markAsCompleted(storyId: String) {
        stories.find { it.id == storyId }?.let { story ->
            update(story.copy(isCompleted = true))
        }
    }
    
    override suspend fun toggleFavorite(storyId: String) {
        stories.find { it.id == storyId }?.let { story ->
            update(story.copy(isFavorite = !story.isFavorite))
        }
    }
}

class MockUserProgressDao : UserProgressDao {
    private val progressList = mutableListOf<UserProgressEntity>()
    
    override suspend fun insert(progress: UserProgressEntity) {
        progressList.add(progress)
    }
    
    override suspend fun update(progress: UserProgressEntity) {
        progressList.removeIf { it.id == progress.id }
        progressList.add(progress)
    }
    
    override suspend fun getUserProgress(userId: String): UserProgressEntity? {
        return progressList.find { it.userId == userId }
    }
    
    override fun getUserProgressFlow(userId: String): Flow<UserProgressEntity?> {
        return flowOf(progressList.find { it.userId == userId })
    }
    
    override suspend fun incrementStoriesCompleted(userId: String) {
        getUserProgress(userId)?.let { progress ->
            update(progress.copy(storiesCompleted = progress.storiesCompleted + 1))
        }
    }
    
    override suspend fun updateTotalMinutesSpent(userId: String, minutes: Int) {
        getUserProgress(userId)?.let { progress ->
            update(progress.copy(totalMinutesSpent = progress.totalMinutesSpent + minutes))
        }
    }
    
    override suspend fun updateLastActiveDate(userId: String, date: Date) {
        getUserProgress(userId)?.let { progress ->
            update(progress.copy(lastActiveDate = date))
        }
    }
}

class MockDailyProgressDao : DailyProgressDao {
    private val dailyProgressList = mutableListOf<DailyProgressEntity>()
    
    override suspend fun insert(progress: DailyProgressEntity) {
        dailyProgressList.add(progress)
    }
    
    override suspend fun update(progress: DailyProgressEntity) {
        dailyProgressList.removeIf { it.date == progress.date && it.userId == progress.userId }
        dailyProgressList.add(progress)
    }
    
    override suspend fun getDailyProgress(userId: String, date: Date): DailyProgressEntity? {
        return dailyProgressList.find { it.userId == userId && it.date == date }
    }
    
    override fun getDailyProgressForWeek(userId: String, startDate: Date, endDate: Date): Flow<List<DailyProgressEntity>> {
        return flowOf(dailyProgressList.filter { 
            it.userId == userId && it.date >= startDate && it.date <= endDate 
        })
    }
    
    override suspend fun recordStoryCompletion(userId: String, date: Date, storyId: String) {
        val progress = getDailyProgress(userId, date) ?: DailyProgressEntity(
            userId = userId,
            date = date,
            storiesCompleted = 0,
            minutesSpent = 0,
            activitiesCompleted = listOf()
        )
        update(progress.copy(
            storiesCompleted = progress.storiesCompleted + 1,
            activitiesCompleted = progress.activitiesCompleted + storyId
        ))
    }
}

class MockAuditLogDao : AuditLogDao {
    private val logs = mutableListOf<AuditLogEntity>()
    
    override suspend fun insert(log: AuditLogEntity) {
        logs.add(log)
    }
    
    override suspend fun insertAll(logs: List<AuditLogEntity>) {
        this.logs.addAll(logs)
    }
    
    override fun getLogsForUser(userId: String): Flow<List<AuditLogEntity>> {
        return flowOf(logs.filter { it.userId == userId })
    }
    
    override fun getLogsByDateRange(startDate: Long, endDate: Long): Flow<List<AuditLogEntity>> {
        return flowOf(logs.filter { it.timestamp in startDate..endDate })
    }
    
    override suspend fun deleteOldLogs(beforeDate: Long) {
        logs.removeIf { it.timestamp < beforeDate }
    }
}