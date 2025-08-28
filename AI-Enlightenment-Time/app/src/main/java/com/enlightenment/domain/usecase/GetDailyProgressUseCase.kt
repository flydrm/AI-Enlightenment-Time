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
        return userProgressRepository.getDailyProgress(date)
    }
    
    fun observeProgress(date: LocalDate): Flow<DailyProgress?> {
        return userProgressRepository.observeDailyProgress(date)
    }
}