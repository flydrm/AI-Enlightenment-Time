package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.WeeklyProgress
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyProgressUseCase @Inject constructor(
    private val userProgressRepository: UserProgressRepository
) {
    suspend operator fun invoke(weekStart: LocalDate): Result<WeeklyProgress> {
        return userProgressRepository.getWeeklyProgress(weekStart)
    }
    
    fun observeWeeklyProgress(): Flow<WeeklyProgress> {
        return userProgressRepository.observeWeeklyProgress()
    }
}