package com.enlightenment.domain.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.Achievement
import java.util.Date
import kotlinx.coroutines.flow.Flow



interface AchievementRepository {
    fun getAchievements(userId: String): Flow<List<Achievement>>
    suspend fun unlockAchievement(userId: String, achievementId: String)
    suspend fun getAchievementsUnlockedBetween(userId: String, startDate: Date, endDate: Date): List<Achievement>
    suspend fun getTotalLearningTime(userId: String): Int
    suspend fun getDailyStreak(userId: String): Int
    suspend fun getTodayActivities(userId: String): List<String>
}
