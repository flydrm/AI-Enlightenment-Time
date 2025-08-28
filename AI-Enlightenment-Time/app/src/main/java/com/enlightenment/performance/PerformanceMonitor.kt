package com.enlightenment.performance

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.MainThread
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



/**
 * 性能监控器
 * 监控应用性能指标并提供优化建议
 */
class PerformanceMonitor(
    private val memoryOptimizer: MemoryOptimizer
) {
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val FRAME_TIME_THRESHOLD_MS = 16 // 60 FPS
        private const val SLOW_METHOD_THRESHOLD_MS = 100
    }
    
    private val mainHandler = Handler(Looper.getMainLooper())
    private val monitorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 性能指标
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics
    
    // 帧率监控
    private var frameCallback: Choreographer.FrameCallback? = null
    private var lastFrameTime = 0L
    private val frameTimeHistory = mutableListOf<Long>()
    
    /**
     * 开始监控
     */
    fun startMonitoring() {
        // 监控内存
        monitorMemory()
        
        // 监控帧率
        monitorFrameRate()
        
        // 监控主线程阻塞
        monitorMainThreadBlocking()
    }
    
    /**
     * 停止监控
     */
    fun stopMonitoring() {
        monitorScope.cancel()
        frameCallback?.let { 
            android.view.Choreographer.getInstance().removeFrameCallback(it)
        }
    }
    
    /**
     * 测量方法执行时间
     */
    inline fun <T> measureMethod(methodName: String, block: () -> T): T {
        val startTime = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            if (duration > SLOW_METHOD_THRESHOLD_MS) {
                Log.w(TAG, "Slow method detected: $methodName took ${duration}ms")
            }
            recordMethodTime(methodName, duration)
        }
    }
    
    /**
     * 测量协程执行时间
     */
    suspend fun <T> measureSuspend(methodName: String, block: suspend () -> T): T {
        val duration = measureTimeMillis {
            return block()
        }
        
        if (duration > SLOW_METHOD_THRESHOLD_MS) {
            Log.w(TAG, "Slow suspend method detected: $methodName took ${duration}ms")
        }
        recordMethodTime(methodName, duration)
        
        return block()
    }
    
    private fun monitorMemory() {
        monitorScope.launch {
            while (isActive) {
                val memoryStatus = memoryOptimizer.getMemoryInfo()
                
                _performanceMetrics.value = _performanceMetrics.value.copy(
                    memoryUsageMB = memoryStatus.usedMemoryMB,
                    memoryUsagePercent = memoryStatus.memoryUsagePercent,
                    isLowMemory = memoryStatus.lowMemory
                )
                
                if (memoryStatus.lowMemory) {
                    Log.w(TAG, "Low memory detected! Used: ${memoryStatus.usedMemoryMB}MB")
                    memoryOptimizer.optimizeMemory()
                }
                
                delay(5000) // 每5秒检查一次
            }
        }
    }
    
    private fun monitorFrameRate() {
        frameCallback = object : android.view.Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTime != 0L) {
                    val frameTime = (frameTimeNanos - lastFrameTime) / 1_000_000 // 转换为毫秒
                    frameTimeHistory.add(frameTime)
                    
                    // 保持最近60帧的历史
                    if (frameTimeHistory.size > 60) {
                        frameTimeHistory.removeAt(0)
                    }
                    
                    // 计算平均帧率
                    val avgFrameTime = frameTimeHistory.average()
                    val fps = if (avgFrameTime > 0) (1000 / avgFrameTime).toInt() else 60
                    
                    // 检测卡顿
                    if (frameTime > FRAME_TIME_THRESHOLD_MS * 2) {
                        Log.w(TAG, "Frame drop detected! Frame time: ${frameTime}ms")
                        recordFrameDrop(frameTime)
                    }
                    
                    _performanceMetrics.value = _performanceMetrics.value.copy(
                        currentFPS = fps,
                        averageFrameTime = avgFrameTime.toFloat()
                    )
                }
                
                lastFrameTime = frameTimeNanos
                android.view.Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
    }
    
    private fun monitorMainThreadBlocking() {
        monitorScope.launch {
            while (isActive) {
                val startTime = SystemClock.elapsedRealtime()
                
                withContext(Dispatchers.Main) {
                    // 在主线程上执行一个简单任务
                    // 如果主线程被阻塞，这个任务会延迟执行
                }
                
                val duration = SystemClock.elapsedRealtime() - startTime
                if (duration > 100) {
                    Log.w(TAG, "Main thread might be blocked! Task took ${duration}ms")
                }
                
                delay(1000) // 每秒检查一次
            }
        }
    }
    
    private fun recordMethodTime(methodName: String, duration: Long) {
        _performanceMetrics.value = _performanceMetrics.value.copy(
            slowMethods = _performanceMetrics.value.slowMethods + SlowMethod(methodName, duration)
        )
    }
    
    private fun recordFrameDrop(frameTime: Long) {
        _performanceMetrics.value = _performanceMetrics.value.copy(
            frameDrops = _performanceMetrics.value.frameDrops + 1,
            lastFrameDropTime = frameTime
        )
    }
    
    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): String {
        val metrics = _performanceMetrics.value
        return buildString {
            appendLine("=== Performance Report ===")
            appendLine("Memory Usage: ${metrics.memoryUsageMB}MB (${metrics.memoryUsagePercent}%)")
            appendLine("FPS: ${metrics.currentFPS} (avg frame time: ${metrics.averageFrameTime}ms)")
            appendLine("Frame Drops: ${metrics.frameDrops}")
            appendLine("Low Memory: ${metrics.isLowMemory}")
            
            if (metrics.slowMethods.isNotEmpty()) {
                appendLine("\nSlow Methods:")
                metrics.slowMethods.takeLast(10).forEach { method ->
                    appendLine("  - ${method.name}: ${method.duration}ms")
                }
            }
        }
    }
}
/**
 * 性能指标数据类
 */
data class PerformanceMetrics(
    val memoryUsageMB: Float = 0f,
    val memoryUsagePercent: Float = 0f,
    val currentFPS: Int = 60,
    val averageFrameTime: Float = 16.67f,
    val frameDrops: Int = 0,
    val lastFrameDropTime: Long = 0,
    val isLowMemory: Boolean = false,
    val slowMethods: List<SlowMethod> = emptyList()
)
data class SlowMethod(
    val name: String,
    val duration: Long
)
