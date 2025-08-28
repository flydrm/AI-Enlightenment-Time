package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.DailyProgress
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailyProgressUseCase @Inject constructor(
    private val userProgressRepository: UserProgressRepository
) {
    suspend operator fun invoke(date: LocalDate): Result<DailyProgress> {
        return try {
            val dateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
            val progress = userProgressRepository.getDailyProgress(dateMillis)
            Result.success(progress ?: DailyProgress(dateMillis))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeProgress(date: LocalDate): Flow<DailyProgress?> {
        val dateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000
        return userProgressRepository.observeDailyProgress(dateMillis)
    }
}