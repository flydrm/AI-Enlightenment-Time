package com.enlightenment.di

import android.content.Context
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.network.HttpClient
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.data.repository.StoryRepositoryImpl
import com.enlightenment.data.repository.UserProgressRepositoryImpl
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository
import com.enlightenment.domain.usecase.*
import com.enlightenment.multimedia.audio.AudioManager
import com.enlightenment.multimedia.audio.AudioManagerImpl
import com.enlightenment.multimedia.camera.CameraManager
import com.enlightenment.multimedia.camera.CameraManagerImpl
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import com.enlightenment.performance.StartupOptimizer
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.SecureStorage
import com.enlightenment.security.SecurityManager
import com.enlightenment.security.SecurityManagerImpl

/**
 * 手动依赖注入容器
 * 用于替代Hilt，解决编译问题
 */
object DIContainer {
    
    private lateinit var applicationContext: Context
    
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
    
    // 数据库
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }
    
    // 网络
    val httpClient: HttpClient by lazy {
        HttpClient(applicationContext)
    }
    
    // 偏好设置
    val userPreferences: UserPreferences by lazy {
        UserPreferences(applicationContext)
    }
    
    // Repository
    val storyRepository: StoryRepository by lazy {
        StoryRepositoryImpl(database.storyDao(), userPreferences)
    }
    
    val userProgressRepository: UserProgressRepository by lazy {
        UserProgressRepositoryImpl(database.userProgressDao(), database.dailyProgressDao())
    }
    
    // Use Cases
    val generateStoryUseCase: GenerateStoryUseCase by lazy {
        GenerateStoryUseCase(storyRepository)
    }
    
    val completeStoryUseCase: CompleteStoryUseCase by lazy {
        CompleteStoryUseCase(userProgressRepository)
    }
    
    val getDailyProgressUseCase: GetDailyProgressUseCase by lazy {
        GetDailyProgressUseCase(userProgressRepository)
    }
    
    val getWeeklyProgressUseCase: GetWeeklyProgressUseCase by lazy {
        GetWeeklyProgressUseCase(userProgressRepository)
    }
    
    // 多媒体
    val cameraManager: CameraManager by lazy {
        CameraManagerImpl(applicationContext)
    }
    
    val audioManager: AudioManager by lazy {
        AudioManagerImpl(applicationContext)
    }
    
    // 性能优化
    val startupOptimizer: StartupOptimizer by lazy {
        StartupOptimizer(applicationContext)
    }
    
    val memoryOptimizer: MemoryOptimizer by lazy {
        MemoryOptimizer(applicationContext)
    }
    
    val performanceMonitor: PerformanceMonitor by lazy {
        PerformanceMonitor(memoryOptimizer)
    }
    
    // 安全
    val secureStorage: SecureStorage by lazy {
        SecureStorage(applicationContext)
    }
    
    val securityManager: SecurityManager by lazy {
        SecurityManagerImpl(applicationContext)
    }
    
    val auditLogger: AuditLogger by lazy {
        AuditLogger(applicationContext, database, secureStorage)
    }
}