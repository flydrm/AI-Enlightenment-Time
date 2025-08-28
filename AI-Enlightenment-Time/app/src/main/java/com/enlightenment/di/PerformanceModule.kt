package com.enlightenment.di

import android.content.Context
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import com.enlightenment.performance.StartupOptimizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 性能优化相关的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {
    
    @Provides
    @Singleton
    fun provideMemoryOptimizer(
        @ApplicationContext context: Context
    ): MemoryOptimizer {
        return MemoryOptimizer(context)
    }
    
    @Provides
    @Singleton
    fun provideStartupOptimizer(
        @ApplicationContext context: Context
    ): StartupOptimizer {
        return StartupOptimizer(context)
    }
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(
        memoryOptimizer: MemoryOptimizer
    ): PerformanceMonitor {
        return PerformanceMonitor(memoryOptimizer)
    }
}