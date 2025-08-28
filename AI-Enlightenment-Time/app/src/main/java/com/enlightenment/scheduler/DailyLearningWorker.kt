package com.enlightenment.scheduler

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enlightenment.di.DIContainer
import com.enlightenment.presentation.MainActivityNoHilt
import com.enlightenment.R
import kotlinx.coroutines.flow.first



class DailyLearningWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            sendDailyReminder()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun sendDailyReminder() {
        val userPreferences = DIContainer.userPreferences
        
        // 检查是否启用了提醒
        val isReminderEnabled = true // 简化实现
        if (!isReminderEnabled) {
            return
        }
        
        // 获取用户进度
        val userProgressRepository = DIContainer.userProgressRepository
        val progress = userProgressRepository.getUserProgress()
        
        // 根据进度生成个性化消息
        val message = generatePersonalizedMessage(progress?.storiesCompleted ?: 0)
        
        // 发送通知
        sendNotification(
            title = inputData.getString("title") ?: "学习时间到了！",
            content = message
        )
    }
    
    private fun generatePersonalizedMessage(storiesCompleted: Int): String {
        return when {
            storiesCompleted == 0 -> "快来开始今天的学习之旅吧！"
            storiesCompleted < 5 -> "你已经完成了${storiesCompleted}个故事，继续加油！"
            storiesCompleted < 10 -> "太棒了！已经完成${storiesCompleted}个故事了！"
            else -> "学习小达人！已经完成${storiesCompleted}个故事啦！"
        }
    }
    
    private fun sendNotification(title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 创建通知渠道（Android O及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "学习提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日学习提醒通知"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // 创建点击通知的Intent
        val intent = Intent(applicationContext, MainActivityNoHilt::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_media_play,
                "开始学习",
                pendingIntent
            )
            .build()
        
        // 发送通知
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        const val CHANNEL_ID = "daily_learning_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
