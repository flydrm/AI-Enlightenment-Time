package com.enlightenment.data.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.Achievement
import com.enlightenment.domain.repository.AchievementRepository
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf



class AchievementRepositoryImpl @Inject constructor() : AchievementRepository {
    override fun getAchievements(userId: String): Flow<List<Achievement>> = flowOf(emptyList())
    override suspend fun unlockAchievement(userId: String, achievementId: String) {}
    override suspend fun getAchievementsUnlockedBetween(userId: String, startDate: Date, endDate: Date): List<Achievement> = emptyList()
    override suspend fun getTotalLearningTime(userId: String): Int = 0
    override suspend fun getDailyStreak(userId: String): Int = 0
    override suspend fun getTodayActivities(userId: String): List<String> = emptyList()
}
