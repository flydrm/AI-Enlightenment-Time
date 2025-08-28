package com.enlightenment.di

import android.content.Context
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import com.enlightenment.performance.StartupOptimizer

/**
 * 性能优化相关的依赖注入模块
 */


object PerformanceModule {
    
    
    
    fun provideMemoryOptimizer(
        context: Context
    ): MemoryOptimizer {
        return MemoryOptimizer(context)
    }
    
    
    
    fun provideStartupOptimizer(
        context: Context
    ): StartupOptimizer {
        return StartupOptimizer(context)
    }
    
    
    
    fun providePerformanceMonitor(
        memoryOptimizer: MemoryOptimizer
    ): PerformanceMonitor {
        return PerformanceMonitor(memoryOptimizer)
    }
}