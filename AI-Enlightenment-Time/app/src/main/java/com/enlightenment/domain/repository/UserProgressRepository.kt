package com.enlightenment.domain.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.DailyProgress
import com.enlightenment.domain.model.UserProgress
import java.util.Date
import kotlinx.coroutines.flow.Flow



interface UserProgressRepository {
    suspend fun createUserProgress(userProgress: UserProgress)
    suspend fun updateUserProgress(userProgress: UserProgress)
    suspend fun getUserProgress(): UserProgress?
    fun getUserProgressFlow(): Flow<UserProgress?>
    suspend fun incrementStoriesCompleted(userId: String)
    suspend fun updateTotalMinutesSpent(userId: String, minutes: Int)
    suspend fun updateLastActiveDate(userId: String, date: Date)
    suspend fun getWeeklyProgress(): List<DailyProgress>
}
