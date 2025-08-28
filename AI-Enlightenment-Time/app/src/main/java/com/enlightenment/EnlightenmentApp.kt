package com.enlightenment

import android.app.Application
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import com.enlightenment.performance.StartupOptimizer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EnlightenmentApp : Application() {
    
    @Inject
    lateinit var startupOptimizer: StartupOptimizer
    
    @Inject
    lateinit var memoryOptimizer: MemoryOptimizer
    
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    
    override fun onCreate() {
        super.onCreate()
        
        // 执行启动优化
        startupOptimizer.optimize()
        
        // 开始性能监控（仅在调试模式）
        if (BuildConfig.DEBUG) {
            performanceMonitor.startMonitoring()
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        startupOptimizer.onLowMemory()
        memoryOptimizer.optimizeMemory()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        memoryOptimizer.onTrimMemory(level)
    }
}