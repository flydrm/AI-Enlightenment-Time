package com.enlightenment

import android.app.Application
import com.enlightenment.di.DIContainer

/**
 * 不使用Hilt的Application类
 */
class EnlightenmentAppNoHilt : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化依赖注入容器
        DIContainer.init(this)
        
        // 执行启动优化
        DIContainer.startupOptimizer.optimize()
        
        // 在调试模式下启动性能监控
        if (BuildConfig.DEBUG) {
            DIContainer.performanceMonitor.startMonitoring()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        DIContainer.memoryOptimizer.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        DIContainer.memoryOptimizer.onTrimMemory(level)
    }
}