package com.enlightenment.scheduler

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enlightenment.di.DIContainer
import java.util.*
import kotlinx.coroutines.flow.first



class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            generateWeeklyReport()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun generateWeeklyReport() {
        val userProgressRepository = DIContainer.userProgressRepository
        
        val endDate = Date()
        val startDate = Date(endDate.time - 7 * 24 * 60 * 60 * 1000L)
        
        val weeklyProgress = userProgressRepository.getWeeklyProgress()
        
        val totalMinutes = weeklyProgress.sumOf { it.totalMinutesSpent }
        val totalStories = weeklyProgress.sumOf { it.storiesCompleted }
        
        sendNotification(
            title = "本周学习报告",
            content = "本周完成${totalStories}个故事，学习${totalMinutes}分钟"
        )
    }
    
    private fun sendNotification(title: String, content: String) {
        // 发送通知逻辑
    }
}
