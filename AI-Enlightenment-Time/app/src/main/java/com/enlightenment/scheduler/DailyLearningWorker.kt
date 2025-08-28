package com.enlightenment.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enlightenment.MainActivity
import com.enlightenment.R
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.domain.usecase.GenerateStoryUseCase
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.UserAction
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 每日学习提醒工作器
 */
@HiltWorker
class DailyLearningWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val generateStoryUseCase: GenerateStoryUseCase,
    private val auditLogger: AuditLogger
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val CHANNEL_ID = "daily_learning_reminder"
        const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        try {
            // 检查是否启用了提醒
            val isReminderEnabled = userPreferences.isDailyReminderEnabled.first()
            if (!isReminderEnabled) {
                return Result.success()
            }
            
            // 检查今天是否已经学习
            val hasLearnedToday = checkIfLearnedToday()
            if (hasLearnedToday && !inputData.getBoolean("is_test", false)) {
                return Result.success()
            }
            
            // 创建通知渠道
            createNotificationChannel()
            
            // 准备今日学习内容
            val todayTheme = generateTodayTheme()
            val childName = userPreferences.childName.first()
            
            // 发送通知
            showLearningReminder(childName, todayTheme)
            
            // 记录提醒发送
            auditLogger.logUserAction(
                UserAction.APP_LAUNCH,
                "发送每日学习提醒",
                mapOf(
                    "theme" to todayTheme,
                    "scheduled_time" to "${inputData.getInt("scheduled_hour", 9)}:${inputData.getInt("scheduled_minute", 0)}"
                )
            )
            
            return Result.success()
            
        } catch (e: Exception) {
            auditLogger.logError(
                "DAILY_REMINDER_ERROR",
                "发送每日学习提醒失败",
                e.stackTraceToString()
            )
            return Result.retry()
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "每日学习提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒小朋友每天的AI启蒙学习时间"
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 显示学习提醒通知
     */
    private fun showLearningReminder(childName: String, theme: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("from_notification", true)
            putExtra("suggested_theme", theme)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_panda)
            .setContentTitle("🐼 ${childName}，学习时间到啦！")
            .setContentText("今天我们一起探索「$theme」吧！")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("小熊猫已经准备好了精彩的故事和有趣的游戏，快来和我一起学习吧！今天的主题是「$theme」，会有很多好玩的内容等着你哦！"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_play,
                "开始学习",
                pendingIntent
            )
            .build()
        
        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        }
    }
    
    /**
     * 检查今天是否已经学习
     */
    private suspend fun checkIfLearnedToday(): Boolean {
        // 这里应该查询数据库检查今天的学习记录
        // 暂时返回false
        return false
    }
    
    /**
     * 生成今日主题
     */
    private fun generateTodayTheme(): String {
        val themes = listOf(
            "神奇的海洋世界",
            "太空探险记",
            "森林里的小动物",
            "有趣的科学实验",
            "恐龙的秘密",
            "彩虹的故事",
            "音乐的魔法",
            "数字王国历险",
            "勇敢的小英雄",
            "友谊的力量"
        )
        
        // 基于日期选择主题，确保每天不同
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return themes[dayOfYear % themes.size]
    }
}