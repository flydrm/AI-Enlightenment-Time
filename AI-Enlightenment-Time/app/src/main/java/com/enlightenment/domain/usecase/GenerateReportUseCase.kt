package com.enlightenment.domain.usecase

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
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
