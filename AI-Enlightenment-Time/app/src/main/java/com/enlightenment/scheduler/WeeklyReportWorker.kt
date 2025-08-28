package com.enlightenment.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.DataMaskingService
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * 每周进度报告工作器
 * 生成并发送每周学习报告给家长
 */

class WeeklyReportWorker  constructor(
    appContext: Context,
    workerParams: WorkerParameters,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences,
    private val auditLogger: AuditLogger,
    private val dataMaskingService: DataMaskingService
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // 生成报告
            val report = generateWeeklyReport()
            
            // 保存报告
            saveReport(report)
            
            // 发送通知给家长
            notifyParents(report)
            
            // 记录报告生成
            auditLogger.logUserAction(
                com.enlightenment.security.UserAction.APP_LAUNCH,
                "生成每周学习报告",
                mapOf(
                    "week_start" to report.weekStartDate,
                    "week_end" to report.weekEndDate,
                    "total_stories" to report.totalStoriesCompleted.toString(),
                    "total_time" to report.totalLearningMinutes.toString()
                )
            )
            
            Result.success()
            
        } catch (e: Exception) {
            auditLogger.logError(
                "WEEKLY_REPORT_ERROR",
                "生成每周报告失败",
                e.stackTraceToString()
            )
            Result.retry()
        }
    }
    
    /**
     * 生成每周报告
     */
    private suspend fun generateWeeklyReport(): WeeklyReport {
        val calendar = Calendar.getInstance()
        val weekEnd = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, -7)
        val weekStart = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekStartStr = dateFormat.format(weekStart)
        val weekEndStr = dateFormat.format(weekEnd)
        
        // 获取本周数据
        val weekStartMillis = weekStart.time
        val weekEndMillis = weekEnd.time
        
        // 获取完成的故事
        val completedStories = database.storyDao().getCompletedStoriesBetween(weekStartMillis, weekEndMillis)
        
        // 获取学习记录
        val learningRecords = database.dailyProgressDao().getProgressBetween(weekStartMillis, weekEndMillis)
        
        // 获取成就
        val achievements = database.userProgressDao().getAchievementsUnlockedBetween(weekStartMillis, weekEndMillis)
        
        // 计算统计数据
        val totalMinutes = learningRecords.sumOf { it.minutesSpent }
        val averageMinutesPerDay = if (learningRecords.isNotEmpty()) totalMinutes / learningRecords.size else 0
        val daysLearned = learningRecords.size
        
        // 获取最喜欢的主题
        val favoriteThemes = completedStories
            .groupBy { it.category }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key.name }
        
        // 获取学习进步
        val progressHighlights = generateProgressHighlights(learningRecords)
        
        return WeeklyReport(
            weekStartDate = weekStartStr,
            weekEndDate = weekEndStr,
            totalStoriesCompleted = completedStories.size,
            totalLearningMinutes = totalMinutes,
            averageMinutesPerDay = averageMinutesPerDay,
            daysLearned = daysLearned,
            achievementsUnlocked = achievements.size,
            favoriteThemes = favoriteThemes,
            progressHighlights = progressHighlights,
            generatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 生成进步亮点
     */
    private fun generateProgressHighlights(records: List<com.enlightenment.data.local.entity.DailyProgressEntity>): List<String> {
        val highlights = mutableListOf<String>()
        
        // 连续学习天数
        val consecutiveDays = calculateConsecutiveDays(records)
        if (consecutiveDays >= 3) {
            highlights.add("连续学习了 $consecutiveDays 天！真棒！")
        }
        
        // 最长学习时间
        val longestSession = records.maxByOrNull { it.minutesSpent }
        longestSession?.let {
            if (it.minutesSpent >= 20) {
                highlights.add("有一天学习了 ${it.minutesSpent} 分钟，专注力很好！")
            }
        }
        
        // 早起学习
        val earlyBird = records.filter { 
            Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.HOUR_OF_DAY) < 9 
        }
        if (earlyBird.isNotEmpty()) {
            highlights.add("有 ${earlyBird.size} 天早起学习，养成了好习惯！")
        }
        
        return highlights
    }
    
    /**
     * 计算连续学习天数
     */
    private fun calculateConsecutiveDays(records: List<com.enlightenment.data.local.entity.DailyProgressEntity>): Int {
        if (records.isEmpty()) return 0
        
        val sortedDates = records.map { it.date }.sorted()
        var maxConsecutive = 1
        var currentConsecutive = 1
        
        for (i in 1 until sortedDates.size) {
            val dayDiff = (sortedDates[i] - sortedDates[i-1]) / (24 * 60 * 60 * 1000)
            if (dayDiff == 1L) {
                currentConsecutive++
                maxConsecutive = maxOf(maxConsecutive, currentConsecutive)
            } else {
                currentConsecutive = 1
            }
        }
        
        return maxConsecutive
    }
    
    /**
     * 保存报告
     */
    private suspend fun saveReport(report: WeeklyReport) {
        // 将报告保存到数据库或文件系统
        val reportJson = report.toJson()
        val reportFile = java.io.File(
            applicationContext.filesDir,
            "reports/weekly_${report.weekStartDate}_${report.weekEndDate}.json"
        )
        reportFile.parentFile?.mkdirs()
        reportFile.writeText(reportJson)
    }
    
    /**
     * 通知家长
     */
    private suspend fun notifyParents(report: WeeklyReport) {
        // 这里可以实现发送邮件或应用内通知
        // 暂时只记录日志
        val childName = userPreferences.childName.first()
        val maskedName = dataMaskingService.maskChildName(childName)
        
        auditLogger.logUserAction(
            com.enlightenment.security.UserAction.APP_LAUNCH,
            "发送每周报告通知",
            mapOf(
                "child_name" to maskedName,
                "report_week" to "${report.weekStartDate} - ${report.weekEndDate}"
            )
        )
    }
}

/**
 * 每周报告数据类
 */
data class WeeklyReport(
    val weekStartDate: String,
    val weekEndDate: String,
    val totalStoriesCompleted: Int,
    val totalLearningMinutes: Int,
    val averageMinutesPerDay: Int,
    val daysLearned: Int,
    val achievementsUnlocked: Int,
    val favoriteThemes: List<String>,
    val progressHighlights: List<String>,
    val generatedAt: Long
) {
    fun toJson(): String {
        // 简单的JSON序列化
        return """
        {
            "weekStartDate": "$weekStartDate",
            "weekEndDate": "$weekEndDate",
            "totalStoriesCompleted": $totalStoriesCompleted,
            "totalLearningMinutes": $totalLearningMinutes,
            "averageMinutesPerDay": $averageMinutesPerDay,
            "daysLearned": $daysLearned,
            "achievementsUnlocked": $achievementsUnlocked,
            "favoriteThemes": ${favoriteThemes.joinToString(",", "[", "]") { "\"$it\"" }},
            "progressHighlights": ${progressHighlights.joinToString(",", "[", "]") { "\"$it\"" }},
            "generatedAt": $generatedAt
        }
        """.trimIndent()
    }
}