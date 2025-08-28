package com.enlightenment.data.repository

import com.enlightenment.data.local.dao.UserProgressDao
import com.enlightenment.data.local.entity.UserProgressEntity
import com.enlightenment.domain.model.Achievement
import com.enlightenment.domain.model.AchievementCategory
import com.enlightenment.domain.model.DailyProgress
import com.enlightenment.domain.model.UserProgress
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao
) : UserProgressRepository {
    
    companion object {
        private const val DEFAULT_USER_ID = "default_user"
    }
    
    override suspend fun getUserProgress(): Flow<UserProgress> {
        return userProgressDao.getUserProgress(DEFAULT_USER_ID).map { entity ->
            entity?.toDomainModel() ?: createDefaultProgress()
        }
    }
    
    override suspend fun updateProgress(progress: UserProgress) {
        userProgressDao.updateProgress(UserProgressEntity.fromDomainModel(progress))
    }
    
    override suspend fun recordStoryCompletion(storyId: String, timeSpentMinutes: Int) {
        val progress = getUserProgressEntity() ?: createDefaultProgressEntity()
        
        val updatedProgress = progress.copy(
            totalStoriesCompleted = progress.totalStoriesCompleted + 1,
            totalLearningTime = progress.totalLearningTime + timeSpentMinutes,
            completedStoryIds = progress.completedStoryIds + storyId,
            lastActiveDate = System.currentTimeMillis()
        )
        
        userProgressDao.updateProgress(updatedProgress)
    }
    
    override suspend fun recordQuestionAnswer(isCorrect: Boolean) {
        // TODO: Implement daily progress tracking
    }
    
    override suspend fun getDailyProgress(date: Long): DailyProgress? {
        // TODO: Implement daily progress tracking
        return null
    }
    
    override suspend fun getWeeklyProgress(): List<DailyProgress> {
        // TODO: Implement weekly progress tracking
        return emptyList()
    }
    
    override suspend fun getAchievements(): Flow<List<Achievement>> {
        return flow {
            emit(getDefaultAchievements())
        }
    }
    
    override suspend fun unlockAchievement(achievementId: String) {
        val progress = getUserProgressEntity() ?: createDefaultProgressEntity()
        val updatedProgress = progress.copy(
            unlockedAchievements = progress.unlockedAchievements + achievementId
        )
        userProgressDao.updateProgress(updatedProgress)
    }
    
    override suspend fun updateStreak() {
        val progress = getUserProgressEntity() ?: createDefaultProgressEntity()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val lastActiveDate = Calendar.getInstance().apply {
            timeInMillis = progress.lastActiveDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val daysDiff = TimeUnit.MILLISECONDS.toDays(today - lastActiveDate)
        
        val newStreak = when {
            daysDiff == 0L -> progress.currentStreak // Same day
            daysDiff == 1L -> progress.currentStreak + 1 // Consecutive day
            else -> 1 // Streak broken
        }
        
        val longestStreak = maxOf(progress.longestStreak, newStreak)
        
        val updatedProgress = progress.copy(
            currentStreak = newStreak,
            longestStreak = longestStreak,
            lastActiveDate = System.currentTimeMillis()
        )
        
        userProgressDao.updateProgress(updatedProgress)
    }
    
    override suspend fun resetProgress() {
        userProgressDao.deleteProgress(DEFAULT_USER_ID)
    }
    
    private suspend fun getUserProgressEntity(): UserProgressEntity? {
        // For now, we'll create a default if none exists
        // In a real app, you might want to use first() or similar
        return null
    }
    
    private fun createDefaultProgress(): UserProgress {
        return UserProgress(
            userId = DEFAULT_USER_ID,
            totalStoriesCompleted = 0,
            totalLearningTime = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastActiveDate = System.currentTimeMillis(),
            favoriteCategories = emptyList(),
            completedStoryIds = emptySet(),
            unlockedAchievements = emptySet()
        )
    }
    
    private suspend fun createDefaultProgressEntity(): UserProgressEntity {
        val entity = UserProgressEntity.fromDomainModel(createDefaultProgress())
        userProgressDao.insertProgress(entity)
        return entity
    }
    
    private fun getDefaultAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_story",
                name = "初次探索",
                description = "完成第一个故事",
                icon = "🎯",
                requiredCount = 1,
                category = AchievementCategory.STORY
            ),
            Achievement(
                id = "story_master_5",
                name = "故事小达人",
                description = "完成5个故事",
                icon = "📚",
                requiredCount = 5,
                category = AchievementCategory.STORY
            ),
            Achievement(
                id = "weekly_streak",
                name = "坚持一周",
                description = "连续学习7天",
                icon = "🔥",
                requiredCount = 7,
                category = AchievementCategory.CONSISTENCY
            ),
            Achievement(
                id = "explorer",
                name = "小小探索家",
                description = "尝试所有功能模块",
                icon = "🔍",
                requiredCount = 4,
                category = AchievementCategory.EXPLORATION
            )
        )
    }
}