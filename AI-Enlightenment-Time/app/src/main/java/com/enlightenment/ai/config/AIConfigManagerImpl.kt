package com.enlightenment.ai.config

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_config")

/**
 * AI配置管理实现
 */
@Singleton
class AIConfigManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AIConfigManager {

    private val keyAlias = "AIEnlightenmentKeyAlias"
    private val androidKeyStore = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val healthStatusMap = mutableMapOf<AIModelType, ModelHealthStatus>()

    init {
        generateKey()
    }

    override suspend fun updateConfig(
        modelType: AIModelType,
        appKey: String?,
        apiBaseUrl: String?
    ): Result<Unit> {
        return try {
            val currentEnv = getCurrentEnvironment()
            
            context.dataStore.edit { preferences ->
                appKey?.let { key ->
                    // 验证App Key
                    ConfigValidator.validateAppKey(key).getOrThrow()
                    
                    // 加密并存储
                    val encryptedKey = encryptData(key)
                    val keyPrefKey = stringPreferencesKey(
                        AIConfigKeys.getAppKeyKey(modelType, currentEnv)
                    )
                    preferences[keyPrefKey] = encryptedKey
                }
                
                apiBaseUrl?.let { url ->
                    // 验证URL
                    val whitelist = getWhitelist()
                    ConfigValidator.validateApiUrl(url, whitelist).getOrThrow()
                    
                    // 存储URL（明文）
                    val urlPrefKey = stringPreferencesKey(
                        AIConfigKeys.getApiUrlKey(modelType, currentEnv)
                    )
                    preferences[urlPrefKey] = url
                }
            }
            
            // 记录审计日志
            logConfigChange(modelType, appKey != null, apiBaseUrl != null)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConfig(modelType: AIModelType): ModelConfig? {
        val currentEnv = getCurrentEnvironment()
        val preferences = context.dataStore.data.first()
        
        val encryptedKey = preferences[stringPreferencesKey(
            AIConfigKeys.getAppKeyKey(modelType, currentEnv)
        )]
        val apiUrl = preferences[stringPreferencesKey(
            AIConfigKeys.getApiUrlKey(modelType, currentEnv)
        )]
        
        return if (encryptedKey != null && apiUrl != null) {
            try {
                val decryptedKey = decryptData(encryptedKey)
                ModelConfig(
                    model = modelType.name,
                    appKey = decryptedKey,
                    apiBaseUrl = apiUrl,
                    environment = currentEnv
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    override suspend fun testConnection(modelType: AIModelType): Result<ModelHealthStatus> {
        return try {
            val config = getConfig(modelType) 
                ?: return Result.failure(Exception("No configuration found for $modelType"))
            
            // 执行健康检查
            val isHealthy = performHealthCheck(modelType, config)
            
            val status = ModelHealthStatus(
                modelType = modelType,
                isHealthy = isHealthy,
                successRate = if (isHealthy) 1.0f else 0.0f,
                errorRate = if (isHealthy) 0.0f else 1.0f,
                lastSuccessTime = System.currentTimeMillis(),
                lastErrorTime = null,
                inCircuitBreaker = false
            )
            
            healthStatusMap[modelType] = status
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchEnvironment(environment: Environment): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[AIConfigKeys.CURRENT_ENVIRONMENT] = environment.name
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentEnvironment(): Environment {
        val envName = context.dataStore.data
            .map { preferences -> preferences[AIConfigKeys.CURRENT_ENVIRONMENT] }
            .first()
        
        return envName?.let { Environment.valueOf(it) } ?: Environment.PRODUCTION
    }

    override suspend fun getHealthStatus(modelType: AIModelType): ModelHealthStatus {
        return healthStatusMap[modelType] ?: ModelHealthStatus(
            modelType = modelType,
            isHealthy = false,
            successRate = 0f,
            errorRate = 0f,
            lastSuccessTime = null,
            lastErrorTime = null,
            inCircuitBreaker = false
        )
    }

    override suspend fun updateHealthStatus(
        modelType: AIModelType,
        isSuccess: Boolean,
        errorMessage: String?
    ) {
        val currentStatus = getHealthStatus(modelType)
        val now = System.currentTimeMillis()
        
        // 简单的滑动窗口实现（最近10次请求）
        val windowSize = 10
        val newSuccessCount = if (isSuccess) 1 else 0
        val newErrorCount = if (isSuccess) 0 else 1
        
        val updatedSuccessRate = ((currentStatus.successRate * (windowSize - 1)) + newSuccessCount) / windowSize
        val updatedErrorRate = ((currentStatus.errorRate * (windowSize - 1)) + newErrorCount) / windowSize
        
        // 熔断器逻辑：错误率超过50%时触发
        val shouldTriggerCircuitBreaker = updatedErrorRate > 0.5f
        val circuitBreakerUntil = if (shouldTriggerCircuitBreaker) now + 60_000 else null // 60秒熔断
        
        val updatedStatus = currentStatus.copy(
            isHealthy = !shouldTriggerCircuitBreaker && updatedSuccessRate > 0.5f,
            successRate = updatedSuccessRate,
            errorRate = updatedErrorRate,
            lastSuccessTime = if (isSuccess) now else currentStatus.lastSuccessTime,
            lastErrorTime = if (!isSuccess) now else currentStatus.lastErrorTime,
            inCircuitBreaker = shouldTriggerCircuitBreaker || 
                (currentStatus.inCircuitBreaker && currentStatus.circuitBreakerUntil?.let { it > now } == true),
            circuitBreakerUntil = circuitBreakerUntil ?: currentStatus.circuitBreakerUntil
        )
        
        healthStatusMap[modelType] = updatedStatus
    }

    override suspend fun getHealthyModelsForCapability(capability: ModelCapability): List<AIModelType> {
        val modelsWithCapability = ModelCapabilityMapping.getModelsForCapability(capability)
        val currentEnv = getCurrentEnvironment()
        
        return modelsWithCapability.filter { modelType ->
            val config = getConfig(modelType)
            val healthStatus = getHealthStatus(modelType)
            
            config != null && 
            healthStatus.isHealthy && 
            !healthStatus.inCircuitBreaker
        }.sortedByDescending { getHealthStatus(it).successRate }
    }

    override suspend fun clearAllConfigs() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        healthStatusMap.clear()
    }

    // 私有辅助方法

    private fun generateKey() {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                androidKeyStore
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(androidKeyStore)
        keyStore.load(null)
        return keyStore.getKey(keyAlias, null) as SecretKey
    }

    private fun encryptData(data: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val encryption = cipher.doFinal(data.toByteArray())
        
        // 组合IV和加密数据
        val combined = ByteArray(iv.size + encryption.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryption, 0, combined, iv.size, encryption.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    private fun decryptData(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        
        // 提取IV和加密数据
        val iv = ByteArray(12) // GCM IV 大小固定为12字节
        val encrypted = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        System.arraycopy(combined, iv.size, encrypted, 0, encrypted.size)
        
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }

    private suspend fun getWhitelist(): List<String> {
        val whitelistStr = context.dataStore.data
            .map { preferences -> preferences[AIConfigKeys.DOMAIN_WHITELIST] }
            .first()
        
        return whitelistStr?.split(",")?.map { it.trim() } ?: emptyList()
    }

    private fun logConfigChange(
        modelType: AIModelType,
        keyChanged: Boolean,
        urlChanged: Boolean
    ) {
        // 记录审计日志
        val changes = mutableListOf<String>()
        if (keyChanged) changes.add("API_KEY")
        if (urlChanged) changes.add("API_URL")
        
        val auditLog = """
            |Timestamp: ${System.currentTimeMillis()}
            |Model Type: $modelType
            |Changed Fields: ${changes.joinToString(", ")}
            |User: ${android.os.Process.myUid()}
        """.trimMargin()
        
        // 将审计日志保存到安全存储
        securityManager.saveSecureData(
            "audit_log_${System.currentTimeMillis()}_$modelType",
            auditLog
        )
    }
    
    /**
     * 执行健康检查
     */
    private suspend fun performHealthCheck(modelType: AIModelType, config: AIModelConfig): Boolean {
        return try {
            when (modelType) {
                AIModelType.TEXT_GENERATION -> {
                    // 简单的健康检查：验证配置是否有效
                    config.apiKey.isNotEmpty() && config.apiUrl?.isNotEmpty() == true
                }
                AIModelType.IMAGE_GENERATION -> {
                    // 图像生成模型的健康检查
                    config.apiKey.isNotEmpty() && config.apiUrl?.isNotEmpty() == true
                }
                AIModelType.SPEECH_RECOGNITION -> {
                    // 语音识别模型的健康检查
                    config.apiKey.isNotEmpty()
                }
                AIModelType.TEXT_TO_SPEECH -> {
                    // 文字转语音模型的健康检查
                    config.apiKey.isNotEmpty()
                }
                AIModelType.IMAGE_RECOGNITION -> {
                    // 图像识别模型的健康检查
                    config.apiKey.isNotEmpty() && config.apiUrl?.isNotEmpty() == true
                }
                AIModelType.CONTENT_RERANKING -> {
                    // 内容重排模型的健康检查
                    config.apiKey.isNotEmpty() && config.apiUrl?.isNotEmpty() == true
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}