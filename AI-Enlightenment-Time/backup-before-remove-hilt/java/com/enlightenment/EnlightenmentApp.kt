package com.enlightenment

import android.app.Application
import com.enlightenment.performance.StartupOptimizer
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EnlightenmentApp : Application() {
    
    @Inject lateinit var startupOptimizer: StartupOptimizer
    @Inject lateinit var memoryOptimizer: MemoryOptimizer
    @Inject lateinit var performanceMonitor: PerformanceMonitor

    override fun onCreate() {
        super.onCreate()
        
        // 执行启动优化
        startupOptimizer.optimize()
        
        // 在调试模式下启动性能监控
        if (BuildConfig.DEBUG) {
            performanceMonitor.startMonitoring()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        memoryOptimizer.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        memoryOptimizer.onTrimMemory(level)
    }
}