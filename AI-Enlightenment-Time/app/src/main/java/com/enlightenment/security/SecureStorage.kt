package com.enlightenment.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey



/**
 * 安全存储服务
 * 使用Android加密共享首选项存储敏感数据
 */
class SecureStorage(
    private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_GEMINI_API = "gemini_api_key"
        private const val KEY_OPENAI_API = "openai_api_key"
        private const val KEY_GROK_API = "grok_api_key"
        private const val KEY_QWEN_API = "qwen_api_key"
        private const val KEY_BGE_API = "bge_api_key"
        private const val KEY_PARENT_PIN = "parent_pin"
        private const val KEY_CHILD_PROFILE = "child_profile"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * 存储API密钥
     */
    fun saveApiKey(service: AIService, apiKey: String) {
        val key = when (service) {
            AIService.GEMINI -> KEY_GEMINI_API
            AIService.OPENAI -> KEY_OPENAI_API
            AIService.GROK -> KEY_GROK_API
            AIService.QWEN -> KEY_QWEN_API
            AIService.BGE -> KEY_BGE_API
        }
        encryptedPrefs.edit().putString(key, apiKey).apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(service: AIService): String? {
        val key = when (service) {
            AIService.GEMINI -> KEY_GEMINI_API
            AIService.OPENAI -> KEY_OPENAI_API
            AIService.GROK -> KEY_GROK_API
            AIService.QWEN -> KEY_QWEN_API
            AIService.BGE -> KEY_BGE_API
        }
        return encryptedPrefs.getString(key, null) ?: ""
    }
    
    // 便捷方法
    fun getGeminiApiKey() = getApiKey(AIService.GEMINI)
    fun getOpenAIApiKey() = getApiKey(AIService.OPENAI)
    fun getGrokApiKey() = getApiKey(AIService.GROK)
    fun getQwenApiKey() = getApiKey(AIService.QWEN)
    fun getBGEApiKey() = getApiKey(AIService.BGE)
    
    /**
     * 存储家长PIN码
     */
    fun saveParentPin(pin: String) {
        // 对PIN进行哈希处理后存储
        val hashedPin = hashPin(pin)
        encryptedPrefs.edit().putString(KEY_PARENT_PIN, hashedPin).apply()
    }
    
    /**
     * 验证家长PIN码
     */
    fun verifyParentPin(pin: String): Boolean {
        val storedHash = encryptedPrefs.getString(KEY_PARENT_PIN, null) ?: "" ?: return false
        return hashPin(pin) == storedHash
    }
    
    /**
     * 存储儿童档案信息
     */
    fun saveChildProfile(profile: ChildProfile) {
        val json = profile.toJson()
        encryptedPrefs.edit().putString(KEY_CHILD_PROFILE, json).apply()
    }
    
    /**
     * 获取儿童档案信息
     */
    fun getChildProfile(): ChildProfile? {
        val json = encryptedPrefs.getString(KEY_CHILD_PROFILE, null) ?: "" ?: return null
        return ChildProfile.fromJson(json)
    }
    
    /**
     * 清除所有安全数据
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * 检查是否已配置必要的API密钥
     */
    fun isConfigured(): Boolean {
        return getGeminiApiKey() != null // 至少需要Gemini API密钥
    }
    
    /**
     * PIN码哈希
     */
    private fun hashPin(pin: String): String {
        // 使用SHA-256进行哈希
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.fold("") { str, byte -> str + "%02x".format(byte) }
    }
}
/**
 * AI服务枚举
 */
enum class AIService {
    GEMINI,
    OPENAI,
    GROK,
    QWEN,
    BGE
}
/**
 * 儿童档案
 */
data class ChildProfile(
    val name: String,
    val age: Int,
    val interests: List<String>,
    val educationLevel: String,
    val parentalControls: ParentalControls
) {
    fun toJson(): String {
        // 简单的JSON序列化（实际应使用Gson或其他JSON库）
        return """
            {
                "name": "$name",
                "age": $age,
                "interests": ${interests.joinToString(",", "[", "]") { "\"$it\"" }},
                "educationLevel": "$educationLevel",
                "parentalControls": {
                    "dailyTimeLimit": ${parentalControls.dailyTimeLimitMinutes},
                    "contentFiltering": "${parentalControls.contentFilteringLevel}",
                    "allowVoiceRecording": ${parentalControls.allowVoiceRecording}
                }
            }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): ChildProfile? {
            // 简单的JSON解析（实际应使用Gson或其他JSON库）
            // 这里仅作示例
            return null
        }
    }
}
/**
 * 家长控制设置
 */
data class ParentalControls(
    val dailyTimeLimitMinutes: Int = 15,
    val contentFilteringLevel: ContentFilteringLevel = ContentFilteringLevel.STRICT,
    val allowVoiceRecording: Boolean = true,
    val allowCameraAccess: Boolean = true,
    val requirePinForSettings: Boolean = true
)
/**
 * 内容过滤级别
 */
enum class ContentFilteringLevel {
    STRICT,    // 严格过滤
    MODERATE,  // 适度过滤
    MINIMAL    // 最小过滤
}
