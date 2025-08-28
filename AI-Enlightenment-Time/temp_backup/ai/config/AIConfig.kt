package com.enlightenment.ai.config

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * AI模型配置数据类
 */
data class ModelConfig(
    val model: String,
    val appKey: String,
    val apiBaseUrl: String,
    val environment: Environment = Environment.PRODUCTION,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 环境枚举
 */
enum class Environment {
    DEVELOPMENT,
    TEST,
    PRODUCTION
}

/**
 * AI模型类型枚举
 */
enum class AIModelType {
    GEMINI_2_5_PRO,         // 主对话和故事生成
    GPT_5_PRO,              // 高质量文本生成备选
    QWEN3_EMBEDDING_8B,     // 图像和文本向量化
    BGE_RERANKER_V2_M3,     // 检索结果重排序
    GROK_4_IMAGEGEN         // 图像生成
}

/**
 * 模型能力池
 */
enum class ModelCapability {
    TEXT_GENERATION,        // 文本生成能力
    EMBEDDING,             // 向量化能力
    RERANKING,             // 重排序能力
    IMAGE_GENERATION       // 图像生成能力
}

/**
 * 模型健康状态
 */
data class ModelHealthStatus(
    val modelType: AIModelType,
    val isHealthy: Boolean,
    val successRate: Float,
    val errorRate: Float,
    val lastSuccessTime: Long?,
    val lastErrorTime: Long?,
    val inCircuitBreaker: Boolean = false,
    val circuitBreakerUntil: Long? = null
)

/**
 * AI配置管理接口
 */
interface AIConfigManager {
    /**
     * 更新模型配置
     */
    suspend fun updateConfig(
        modelType: AIModelType,
        appKey: String? = null,
        apiBaseUrl: String? = null
    ): Result<Unit>

    /**
     * 获取模型配置
     */
    suspend fun getConfig(modelType: AIModelType): ModelConfig?

    /**
     * 测试模型连接
     */
    suspend fun testConnection(modelType: AIModelType): Result<ModelHealthStatus>

    /**
     * 切换环境
     */
    suspend fun switchEnvironment(environment: Environment): Result<Unit>

    /**
     * 获取当前环境
     */
    suspend fun getCurrentEnvironment(): Environment

    /**
     * 获取模型健康状态
     */
    suspend fun getHealthStatus(modelType: AIModelType): ModelHealthStatus

    /**
     * 更新模型健康状态
     */
    suspend fun updateHealthStatus(
        modelType: AIModelType,
        isSuccess: Boolean,
        errorMessage: String? = null
    )

    /**
     * 获取同能力池的健康模型
     */
    suspend fun getHealthyModelsForCapability(capability: ModelCapability): List<AIModelType>

    /**
     * 清除所有配置
     */
    suspend fun clearAllConfigs()
}

/**
 * 模型能力映射
 */
object ModelCapabilityMapping {
    val capabilityMap = mapOf(
        AIModelType.GEMINI_2_5_PRO to listOf(ModelCapability.TEXT_GENERATION),
        AIModelType.GPT_5_PRO to listOf(ModelCapability.TEXT_GENERATION),
        AIModelType.QWEN3_EMBEDDING_8B to listOf(ModelCapability.EMBEDDING),
        AIModelType.BGE_RERANKER_V2_M3 to listOf(ModelCapability.RERANKING),
        AIModelType.GROK_4_IMAGEGEN to listOf(ModelCapability.IMAGE_GENERATION)
    )

    fun getModelsForCapability(capability: ModelCapability): List<AIModelType> {
        return capabilityMap.entries
            .filter { it.value.contains(capability) }
            .map { it.key }
    }
}

/**
 * DataStore Keys
 */
object AIConfigKeys {
    val KEY_PREFIX = "ai_config_"
    
    fun getAppKeyKey(modelType: AIModelType, environment: Environment): String {
        return "${KEY_PREFIX}${modelType.name}_${environment.name}_app_key"
    }
    
    fun getApiUrlKey(modelType: AIModelType, environment: Environment): String {
        return "${KEY_PREFIX}${modelType.name}_${environment.name}_api_url"
    }
    
    val CURRENT_ENVIRONMENT = stringPreferencesKey("current_environment")
    val DOMAIN_WHITELIST = stringPreferencesKey("domain_whitelist")
}

/**
 * 配置验证器
 */
object ConfigValidator {
    private val DEFAULT_WHITELIST = listOf(
        "api.openai.com",
        "generativelanguage.googleapis.com",
        "dashscope.aliyuncs.com",
        "api.anthropic.com"
    )

    /**
     * 验证API URL
     */
    fun validateApiUrl(url: String, whitelist: List<String> = DEFAULT_WHITELIST): Result<Unit> {
        return try {
            val uri = java.net.URI(url)
            
            // 必须是HTTPS
            if (uri.scheme != "https") {
                return Result.failure(ConfigValidationException("API URL must use HTTPS"))
            }
            
            // 不能有查询参数或片段
            if (!uri.query.isNullOrEmpty() || !uri.fragment.isNullOrEmpty()) {
                return Result.failure(ConfigValidationException("API URL must not contain query parameters or fragments"))
            }
            
            // 域名白名单检查
            if (whitelist.isNotEmpty() && !whitelist.contains(uri.host)) {
                return Result.failure(ConfigValidationException("Domain not in whitelist: ${uri.host}"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ConfigValidationException("Invalid URL format: ${e.message}"))
        }
    }

    /**
     * 验证App Key格式
     */
    fun validateAppKey(key: String): Result<Unit> {
        return when {
            key.isBlank() -> Result.failure(ConfigValidationException("App key cannot be empty"))
            key.length < 16 -> Result.failure(ConfigValidationException("App key too short"))
            else -> Result.success(Unit)
        }
    }
}

/**
 * 配置验证异常
 */
class ConfigValidationException(message: String) : Exception(message)