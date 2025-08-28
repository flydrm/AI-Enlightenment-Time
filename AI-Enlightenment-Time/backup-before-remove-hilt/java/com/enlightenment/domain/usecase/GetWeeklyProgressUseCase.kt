package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.WeeklyProgress
import com.enlightenment.domain.model.DailyProgress
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import java.util.Calendar

class GetWeeklyProgressUseCase @Inject constructor(
    private val userProgressRepository: UserProgressRepository
) {
    suspend operator fun invoke(): Result<WeeklyProgress> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val weekStart = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.timeInMillis
            
            val dailyProgressList = userProgressRepository.getWeeklyProgress()
            
            val totalStories = dailyProgressList.sumOf { it.storiesCompleted }
            val totalMinutes = dailyProgressList.sumOf { it.learningTimeMinutes }
            val averageStories = if (dailyProgressList.isNotEmpty()) 
                totalStories.toDouble() / dailyProgressList.size else 0.0
            val streakDays = dailyProgressList.count { it.storiesCompleted > 0 }
            
            Result.success(WeeklyProgress(
                weekStart = weekStart,
                weekEnd = weekEnd,
                dailyProgress = dailyProgressList,
                totalStoriesCompleted = totalStories,
                totalLearningMinutes = totalMinutes,
                averageStoriesPerDay = averageStories,
                streakDays = streakDays
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeWeeklyProgress(): Flow<WeeklyProgress> {
        return flow {
            val result = invoke()
            if (result.isSuccess) {
                emit(result.getOrThrow())
            }
        }
    }
}