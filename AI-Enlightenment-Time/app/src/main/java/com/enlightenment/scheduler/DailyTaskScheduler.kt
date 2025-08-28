package com.enlightenment.scheduler

import android.content.Context
import androidx.work.*
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.UserAction
import java.util.Calendar
import java.util.concurrent.TimeUnit



/**
 * 每日任务调度器
 * 负责安排和管理每日15分钟的学习任务
 */
class DailyTaskScheduler(
    private val context: Context,
    private val auditLogger: AuditLogger
) {
    
    companion object {
        const val DAILY_LEARNING_WORK_NAME = "daily_learning_reminder"
        const val DAILY_CLEANUP_WORK_NAME = "daily_cleanup"
        const val WEEKLY_REPORT_WORK_NAME = "weekly_progress_report"
        
        const val TAG_DAILY_TASK = "daily_task"
        const val TAG_CLEANUP = "cleanup"
        const val TAG_REPORT = "report"
        
        // 默认学习时间（早上9点）
        const val DEFAULT_LEARNING_HOUR = 9
        const val DEFAULT_LEARNING_MINUTE = 0
    }
    
    /**
     * 初始化所有定期任务
     */
    fun initializeScheduledTasks() {
        scheduleDailyLearningReminder()
        scheduleDailyCleanup()
        scheduleWeeklyProgressReport()
        
        auditLogger.logUserAction(
            UserAction.APP_LAUNCH,
            "初始化每日任务调度器"
        )
    }
    
    /**
     * 安排每日学习提醒
     */
    fun scheduleDailyLearningReminder(
        hour: Int = DEFAULT_LEARNING_HOUR,
        minute: Int = DEFAULT_LEARNING_MINUTE
    ) {
        // 计算下一次提醒时间
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // 如果今天的时间已过，设置为明天
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        // 创建约束条件
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // 不需要网络
            .setRequiresBatteryNotLow(true) // 电量不低
            .build()
        
        // 创建定期工作请求
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyLearningWorker>(
            1, TimeUnit.DAYS // 每天执行一次
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAG_DAILY_TASK)
            .setInputData(
                workDataOf(
                    "scheduled_hour" to hour,
                    "scheduled_minute" to minute
                )
            )
            .build()
        
        // 调度工作
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_LEARNING_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWorkRequest
            )
        
        auditLogger.logUserAction(
            UserAction.SETTINGS_CHANGE,
            "设置每日学习提醒时间",
            mapOf(
                "hour" to hour.toString(),
                "minute" to minute.toString()
            )
        )
    }
    
    /**
     * 安排每日清理任务
     */
    private fun scheduleDailyCleanup() {
        // 每天凌晨2点执行清理
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(true) // 充电时执行
            .build()
        
        val cleanupRequest = PeriodicWorkRequestBuilder<DailyCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAG_CLEANUP)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_CLEANUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )
    }
    
    /**
     * 安排每周进度报告
     */
    private fun scheduleWeeklyProgressReport() {
        // 每周日晚上8点生成报告
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            
            if (before(currentTime)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络
            .build()
        
        val reportRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
            7, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAG_REPORT)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WEEKLY_REPORT_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reportRequest
            )
    }
    
    /**
     * 取消所有定期任务
     */
    fun cancelAllScheduledTasks() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(DAILY_LEARNING_WORK_NAME)
            cancelUniqueWork(DAILY_CLEANUP_WORK_NAME)
            cancelUniqueWork(WEEKLY_REPORT_WORK_NAME)
        }
        
        auditLogger.logUserAction(
            UserAction.SETTINGS_CHANGE,
            "取消所有定期任务"
        )
    }
    
    /**
     * 获取下一次学习提醒时间
     */
    fun getNextLearningReminderTime(): Long {
        val workInfo = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(DAILY_LEARNING_WORK_NAME)
            .get()
            .firstOrNull()
        
        return workInfo?.nextScheduleTimeMillis ?: 0L
    }
    
    /**
     * 检查是否已启用学习提醒
     */
    fun isLearningReminderEnabled(): Boolean {
        val workInfo = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(DAILY_LEARNING_WORK_NAME)
            .get()
            .firstOrNull()
        
        return workInfo?.state == WorkInfo.State.ENQUEUED
    }
    
    /**
     * 立即触发学习任务（用于测试）
     */
    fun triggerLearningTaskNow() {
        val immediateWork = OneTimeWorkRequestBuilder<DailyLearningWorker>()
            .setInputData(
                workDataOf(
                    "is_test" to true
                )
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(immediateWork)
    }
}
