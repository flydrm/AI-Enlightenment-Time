package com.enlightenment.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.security.AuditLogger
import java.io.File

/**
 * 每日清理工作器
 * 负责清理临时文件、旧日志等
 */

class DailyCleanupWorker  constructor(
    appContext: Context,
    workerParams: WorkerParameters,
    private val database: AppDatabase,
    private val auditLogger: AuditLogger
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val DAYS_TO_KEEP_LOGS = 30
        const val DAYS_TO_KEEP_TEMP_FILES = 7
        const val MAX_CACHE_SIZE_MB = 100
    }
    
    override suspend fun doWork(): Result {
        return try {
            // 1. 清理旧的审计日志
            cleanupOldAuditLogs()
            
            // 2. 清理临时文件
            cleanupTempFiles()
            
            // 3. 清理缓存
            cleanupCache()
            
            // 4. 清理旧的故事数据
            cleanupOldStories()
            
            // 记录清理完成
            auditLogger.logUserAction(
                com.enlightenment.security.UserAction.APP_LAUNCH,
                "每日清理任务完成"
            )
            
            Result.success()
            
        } catch (e: Exception) {
            auditLogger.logError(
                "DAILY_CLEANUP_ERROR",
                "每日清理任务失败",
                e.stackTraceToString()
            )
            Result.retry()
        }
    }
    
    /**
     * 清理旧的审计日志
     */
    private suspend fun cleanupOldAuditLogs() {
        val cutoffTime = System.currentTimeMillis() - (DAYS_TO_KEEP_LOGS * 24 * 60 * 60 * 1000L)
        database.auditLogDao().deleteOldLogs(cutoffTime)
    }
    
    /**
     * 清理临时文件
     */
    private fun cleanupTempFiles() {
        val tempDirs = listOf(
            applicationContext.cacheDir,
            applicationContext.externalCacheDir
        )
        
        tempDirs.filterNotNull().forEach { dir ->
            cleanupDirectory(dir, DAYS_TO_KEEP_TEMP_FILES)
        }
    }
    
    /**
     * 清理缓存
     */
    private fun cleanupCache() {
        val cacheDir = applicationContext.cacheDir
        val maxCacheSize = MAX_CACHE_SIZE_MB * 1024 * 1024 // 转换为字节
        
        val currentSize = calculateDirectorySize(cacheDir)
        if (currentSize > maxCacheSize) {
            // 删除最旧的文件直到缓存大小合理
            val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            
            var deletedSize = 0L
            for (file in files) {
                if (currentSize - deletedSize <= maxCacheSize * 0.8) { // 留20%余量
                    break
                }
                
                val fileSize = if (file.isDirectory) {
                    calculateDirectorySize(file)
                } else {
                    file.length()
                }
                
                if (file.deleteRecursively()) {
                    deletedSize += fileSize
                }
            }
        }
    }
    
    /**
     * 清理旧的故事数据
     */
    private suspend fun cleanupOldStories() {
        // 保留最近30天的故事
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        
        // 获取旧故事
        val oldStories = database.storyDao().getStoriesBeforeDate(cutoffTime)
        
        // 删除相关的图片和音频文件
        oldStories.forEach { story ->
            story.imageUrl?.let { url ->
                if (url.startsWith("file://")) {
                    File(url.removePrefix("file://")).delete()
                }
            }
            story.audioUrl?.let { url ->
                if (url.startsWith("file://")) {
                    File(url.removePrefix("file://")).delete()
                }
            }
        }
        
        // 从数据库删除旧故事
        database.storyDao().deleteStoriesBeforeDate(cutoffTime)
    }
    
    /**
     * 清理目录中的旧文件
     */
    private fun cleanupDirectory(directory: File, daysToKeep: Int) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        
        directory.walkTopDown().forEach { file ->
            if (file.isFile && file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
        
        // 删除空目录
        directory.walkBottomUp().forEach { dir ->
            if (dir.isDirectory && dir != directory && (dir.listFiles()?.isEmpty() == true)) {
                dir.delete()
            }
        }
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirectorySize(directory: File): Long {
        return directory.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}