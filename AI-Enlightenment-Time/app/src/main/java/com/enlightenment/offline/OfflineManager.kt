package com.enlightenment.offline

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.domain.model.Story
import com.enlightenment.security.AuditLogger
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



/**
 * 离线模式管理器
 * 负责管理应用的离线功能和网络状态监控
 */
class OfflineManager(
    private val context: Context,
    private val database: AppDatabase,
    private val auditLogger: AuditLogger
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(NetworkState.UNKNOWN)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private val _offlineMode = MutableStateFlow(OfflineMode.DISABLED)
    val offlineMode: StateFlow<OfflineMode> = _offlineMode.asStateFlow()
    
    init {
        registerNetworkCallback()
        checkInitialNetworkState()
    }
    
    /**
     * 注册网络状态回调
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateNetworkState(NetworkState.CONNECTED)
            }
            
            override fun onLost(network: Network) {
                updateNetworkState(NetworkState.DISCONNECTED)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                if (hasInternet && hasValidated) {
                    updateNetworkState(NetworkState.CONNECTED)
                } else {
                    updateNetworkState(NetworkState.LIMITED)
                }
            }
        })
    }
    
    /**
     * 检查初始网络状态
     */
    private fun checkInitialNetworkState() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val state = when {
            capabilities == null -> NetworkState.DISCONNECTED
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkState.CONNECTED
            else -> NetworkState.LIMITED
        }
        
        updateNetworkState(state)
    }
    
    /**
     * 更新网络状态
     */
    private fun updateNetworkState(state: NetworkState) {
        _networkState.value = state
        
        // 自动切换离线模式
        when (state) {
            NetworkState.DISCONNECTED -> enableOfflineMode(OfflineMode.FULL)
            NetworkState.LIMITED -> enableOfflineMode(OfflineMode.PARTIAL)
            NetworkState.CONNECTED -> disableOfflineMode()
            NetworkState.UNKNOWN -> {}
        }
        
        // 记录网络状态变化
        auditLogger.logUserAction(
            com.enlightenment.security.UserAction.APP_LAUNCH,
            "网络状态变化",
            mapOf("state" to state.name)
        )
    }
    
    /**
     * 启用离线模式
     */
    fun enableOfflineMode(mode: OfflineMode) {
        _offlineMode.value = mode
        
        when (mode) {
            OfflineMode.FULL -> {
                // 完全离线模式：使用本地缓存的所有内容
                auditLogger.logUserAction(
                    com.enlightenment.security.UserAction.SETTINGS_CHANGE,
                    "启用完全离线模式"
                )
            }
            OfflineMode.PARTIAL -> {
                // 部分离线模式：优先使用缓存，必要时才请求网络
                auditLogger.logUserAction(
                    com.enlightenment.security.UserAction.SETTINGS_CHANGE,
                    "启用部分离线模式"
                )
            }
            OfflineMode.DISABLED -> {}
        }
    }
    
    /**
     * 禁用离线模式
     */
    fun disableOfflineMode() {
        _offlineMode.value = OfflineMode.DISABLED
    }
    
    /**
     * 预下载内容以供离线使用
     */
    suspend fun preloadContentForOffline() {
        try {
            // 预下载故事内容
            preloadStories()
            
            // 预下载音频文件
            preloadAudioFiles()
            
            // 预下载图片
            preloadImages()
            
            auditLogger.logUserAction(
                com.enlightenment.security.UserAction.APP_LAUNCH,
                "预下载离线内容完成"
            )
        } catch (e: Exception) {
            auditLogger.logError(
                "OFFLINE_PRELOAD_ERROR",
                "预下载离线内容失败",
                e.stackTraceToString()
            )
        }
    }
    
    /**
     * 预下载故事
     */
    private suspend fun preloadStories() {
        // 获取最受欢迎的故事类别
        val popularCategories = listOf(
            com.enlightenment.domain.model.StoryCategory.ADVENTURE,
            com.enlightenment.domain.model.StoryCategory.FAIRY_TALE,
            com.enlightenment.domain.model.StoryCategory.ANIMAL
        )
        
        // 为每个类别预生成一些故事
        popularCategories.forEach { category ->
            val existingStories = database.storyDao().getStoriesByCategory(category.name)
            if (existingStories.size < 3) {
                // 需要生成更多故事
                // 这里应该调用AI服务生成故事，但在离线模式下使用预设模板
                generateOfflineStory(category)
            }
        }
    }
    
    /**
     * 生成离线故事
     */
    private suspend fun generateOfflineStory(category: com.enlightenment.domain.model.StoryCategory) {
        val templates = OfflineStoryTemplates.getTemplates(category)
        templates.forEach { template ->
            val story = StoryEntity(
                id = "offline_${System.currentTimeMillis()}_${template.hashCode()}",
                title = template.title,
                content = template.content,
                imageUrl = template.imageResourceId,
                audioUrl = null,
                category = category,
                ageGroup = com.enlightenment.domain.model.AgeGroup.THREE_TO_FOUR,
                duration = 5,
                questions = emptyList(),
                isCompleted = false,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                lastPlayedAt = null
            )
            database.storyDao().insertStory(story)
        }
    }
    
    /**
     * 预下载音频文件
     */
    private suspend fun preloadAudioFiles() {
        // 获取需要下载的音频URL列表
        val stories = database.storyDao().getAllStories()
        stories.forEach { story ->
            story.audioUrl?.let { url ->
                if (url.startsWith("http")) {
                    // 下载音频文件到本地
                    // 实际实现中应该使用下载管理器
                }
            }
        }
    }
    
    /**
     * 预下载图片
     */
    private suspend fun preloadImages() {
        // 使用Coil或其他图片加载库的缓存功能
        val stories = database.storyDao().getAllStories()
        stories.forEach { story ->
            story.imageUrl?.let { url ->
                if (url.startsWith("http")) {
                    // 触发图片预加载
                    // coil.enqueue(ImageRequest.Builder(context).data(url).build())
                }
            }
        }
    }
    
    /**
     * 检查是否有足够的离线内容
     */
    suspend fun hasEnoughOfflineContent(): Boolean {
        val storyCount = database.storyDao().getStoryCount()
        return storyCount >= 10 // 至少需要10个故事
    }
    
    /**
     * 获取离线内容统计
     */
    suspend fun getOfflineContentStats(): OfflineContentStats {
        val storyCount = database.storyDao().getStoryCount()
        val completedCount = database.storyDao().getCompletedStoryCount()
        val favoriteCount = database.storyDao().getFavoriteStoryCount()
        
        return OfflineContentStats(
            totalStories = storyCount,
            completedStories = completedCount,
            favoriteStories = favoriteCount,
            cacheSize = calculateCacheSize()
        )
    }
    
    /**
     * 计算缓存大小
     */
    private fun calculateCacheSize(): Long {
        val cacheDir = context.cacheDir
        return calculateDirectorySize(cacheDir)
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirectorySize(directory: java.io.File): Long {
        return directory.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
    
    /**
     * 清理离线缓存
     */
    suspend fun clearOfflineCache(keepFavorites: Boolean = true) {
        if (keepFavorites) {
            // 只删除非收藏的内容
            database.storyDao().deleteNonFavoriteStories()
        } else {
            // 删除所有缓存的故事
            database.storyDao().deleteAllStories()
        }
        
        // 清理缓存文件
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
        
        auditLogger.logUserAction(
            com.enlightenment.security.UserAction.SETTINGS_CHANGE,
            "清理离线缓存",
            mapOf("keep_favorites" to keepFavorites.toString())
        )
    }
}
/**
 * 网络状态
 */
enum class NetworkState {
    UNKNOWN,      // 未知状态
    CONNECTED,    // 已连接
    DISCONNECTED, // 已断开
    LIMITED       // 受限（如仅连接但无互联网）
}
/**
 * 离线模式
 */
enum class OfflineMode {
    DISABLED,  // 禁用（正常模式）
    PARTIAL,   // 部分离线（优先缓存）
    FULL       // 完全离线
}
/**
 * 离线内容统计
 */
data class OfflineContentStats(
    val totalStories: Int,
    val completedStories: Int,
    val favoriteStories: Int,
    val cacheSize: Long
)
