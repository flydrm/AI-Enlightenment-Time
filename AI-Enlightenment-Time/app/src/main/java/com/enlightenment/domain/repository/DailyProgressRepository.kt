package com.enlightenment.domain.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.DailyProgress
import java.util.Date
import kotlinx.coroutines.flow.Flow



interface DailyProgressRepository {
    suspend fun getDailyProgress(userId: String, date: Date): DailyProgress?
    fun getDailyProgressForWeek(userId: String, startDate: Date, endDate: Date): Flow<List<DailyProgress>>
    suspend fun recordActivity(userId: String, activityType: String, duration: Int)
}
