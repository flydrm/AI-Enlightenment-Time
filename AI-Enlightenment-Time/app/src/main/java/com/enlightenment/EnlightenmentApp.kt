package com.enlightenment

import android.app.Application
import com.enlightenment.performance.MemoryOptimizer
import com.enlightenment.performance.PerformanceMonitor
import com.enlightenment.performance.StartupOptimizer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EnlightenmentApp : Application() {
    
    // 暂时注释掉性能优化相关的依赖注入，先确保应用能够编译
    // @Inject
    // lateinit var startupOptimizer: StartupOptimizer
    
    // @Inject
    // lateinit var memoryOptimizer: MemoryOptimizer
    
    // @Inject
    // lateinit var performanceMonitor: PerformanceMonitor
    
    override fun onCreate() {
        super.onCreate()
        
        // 暂时禁用性能优化
        // startupOptimizer.optimize()
        
        // 暂时禁用性能监控
        // if (com.enlightenment.BuildConfig.DEBUG) {
        //     performanceMonitor.startMonitoring()
        // }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        // 暂时禁用
        // startupOptimizer.onLowMemory()
        // memoryOptimizer.optimizeMemory()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // 暂时禁用
        // memoryOptimizer.onTrimMemory(level)
    }
}