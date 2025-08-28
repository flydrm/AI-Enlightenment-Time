package com.enlightenment.performance

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.collection.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内存优化管理器
 * 负责监控和优化应用内存使用
 */
@Singleton
class MemoryOptimizer @Inject constructor(
    private val context: Context
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()
    
    // 图片内存缓存
    private val imageCache: LruCache<String, Bitmap>
    
    // 对象缓存池
    private val objectPools = mutableMapOf<Class<*>, MutableList<WeakReference<Any>>>()
    
    init {
        // 获取可用内存
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        // 使用1/8的可用内存作为图片缓存
        val cacheSize = maxMemory / 8
        
        imageCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // 返回图片占用的内存大小（KB）
                return bitmap.byteCount / 1024
            }
            
            override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
                if (evicted && !oldValue.isRecycled) {
                    // 被移除的图片可以回收
                    oldValue.recycle()
                }
            }
        }
    }
    
    /**
     * 获取当前内存使用情况
     */
    fun getMemoryInfo(): MemoryStatus {
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        return MemoryStatus(
            totalMemory = totalMemory,
            freeMemory = freeMemory,
            usedMemory = usedMemory,
            maxMemory = maxMemory,
            lowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold,
            memoryClass = activityManager.memoryClass,
            largeMemoryClass = activityManager.largeMemoryClass
        )
    }
    
    /**
     * 检查是否处于低内存状态
     */
    fun isLowMemory(): Boolean {
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }
    
    /**
     * 优化内存使用
     */
    fun optimizeMemory() {
        CoroutineScope(Dispatchers.Default).launch {
            // 1. 清理图片缓存
            trimImageCache()
            
            // 2. 清理对象池
            cleanObjectPools()
            
            // 3. 请求垃圾回收
            System.gc()
            
            // 4. 如果仍然低内存，执行更激进的清理
            if (isLowMemory()) {
                aggressiveCleanup()
            }
        }
    }
    
    /**
     * 缓存图片
     */
    fun cacheImage(key: String, bitmap: Bitmap) {
        imageCache.put(key, bitmap)
    }
    
    /**
     * 获取缓存的图片
     */
    fun getCachedImage(key: String): Bitmap? {
        return imageCache.get(key)
    }
    
    /**
     * 对象池：复用对象以减少内存分配
     */
    inline fun <reified T : Any> obtain(): T? {
        val clazz = T::class.java
        val pool = objectPools[clazz]
        
        pool?.iterator()?.let { iterator ->
            while (iterator.hasNext()) {
                val ref = iterator.next()
                val obj = ref.get()
                if (obj != null) {
                    iterator.remove()
                    @Suppress("UNCHECKED_CAST")
                    return obj as T
                } else {
                    iterator.remove()
                }
            }
        }
        
        return null
    }
    
    /**
     * 回收对象到对象池
     */
    fun recycle(obj: Any) {
        val clazz = obj.javaClass
        val pool = objectPools.getOrPut(clazz) { mutableListOf() }
        
        // 限制池大小
        if (pool.size < 10) {
            pool.add(WeakReference(obj))
        }
    }
    
    private fun trimImageCache() {
        val currentSize = imageCache.size()
        val maxSize = imageCache.maxSize()
        
        if (currentSize > maxSize * 0.75) {
            // 当缓存使用超过75%时，trim到50%
            imageCache.trimToSize(maxSize / 2)
        }
    }
    
    private fun cleanObjectPools() {
        objectPools.forEach { (_, pool) ->
            pool.removeAll { it.get() == null }
        }
    }
    
    private fun aggressiveCleanup() {
        // 清空图片缓存
        imageCache.evictAll()
        
        // 清空对象池
        objectPools.clear()
        
        // 触发更激进的垃圾回收
        System.gc()
        System.runFinalization()
        System.gc()
    }
    
    /**
     * 监听内存警告
     */
    fun onTrimMemory(level: Int) {
        when (level) {
            android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI被隐藏，可以释放UI相关资源
                trimImageCache()
            }
            
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // 内存较低，进行适度清理
                optimizeMemory()
            }
            
            android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE,
            android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // 内存严重不足，进行激进清理
                aggressiveCleanup()
            }
        }
    }
}

/**
 * 内存状态信息
 */
data class MemoryStatus(
    val totalMemory: Long,
    val freeMemory: Long,
    val usedMemory: Long,
    val maxMemory: Long,
    val lowMemory: Boolean,
    val threshold: Long,
    val memoryClass: Int,
    val largeMemoryClass: Int
) {
    val usedMemoryMB: Float get() = usedMemory / 1024f / 1024f
    val totalMemoryMB: Float get() = totalMemory / 1024f / 1024f
    val maxMemoryMB: Float get() = maxMemory / 1024f / 1024f
    val memoryUsagePercent: Float get() = (usedMemory.toFloat() / totalMemory) * 100
}