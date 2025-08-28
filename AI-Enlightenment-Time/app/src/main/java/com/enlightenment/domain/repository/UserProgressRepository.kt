package com.enlightenment.domain.repository

import com.enlightenment.domain.model.UserProgress
import com.enlightenment.domain.model.DailyProgress
import com.enlightenment.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface UserProgressRepository {
    suspend fun getUserProgress(): Flow<UserProgress>
    
    suspend fun updateProgress(progress: UserProgress)
    
    suspend fun recordStoryCompletion(storyId: String, timeSpentMinutes: Int)
    
    suspend fun recordQuestionAnswer(isCorrect: Boolean)
    
    suspend fun getDailyProgress(date: Long): DailyProgress?
    
    suspend fun getWeeklyProgress(): List<DailyProgress>
    
    suspend fun getAchievements(): Flow<List<Achievement>>
    
    suspend fun unlockAchievement(achievementId: String)
    
    suspend fun updateStreak()
    
    suspend fun resetProgress()
    
    fun observeDailyProgress(date: Long): Flow<DailyProgress?>
    
    suspend fun addPoints(userId: String, points: Int)
}