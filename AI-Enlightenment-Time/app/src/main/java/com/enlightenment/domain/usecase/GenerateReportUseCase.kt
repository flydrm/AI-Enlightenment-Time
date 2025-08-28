package com.enlightenment.domain.usecase

import com.enlightenment.domain.repository.DailyProgressRepository
import com.enlightenment.domain.repository.UserProgressRepository
import javax.inject.Inject



class GenerateReportUseCase @Inject constructor(
    private val userProgressRepository: UserProgressRepository,
    private val dailyProgressRepository: DailyProgressRepository
) {
    suspend fun generateWeeklyReport(userId: String): String {
        // 生成周报逻辑
        return "本周学习报告"
    }
}
