package com.enlightenment.ai.model.impl

import com.enlightenment.ai.model.TextGenerationModel
import com.enlightenment.data.network.api.*
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



/**
 * Gemini文本生成模型实现
 */
class GeminiTextGenerationModel(
    private val geminiApi: GeminiApi,
    private val secureStorage: SecureStorage
) : TextGenerationModel {
    
    override val name = "GEMINI-2.5-PRO"
    override val version = "2.5"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        // 验证API密钥是否存在
        val apiKey = secureStorage.getGeminiApiKey()
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("Gemini API key not configured")
        }
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getGeminiApiKey()
            ?: throw IllegalStateException("Gemini API key not available")
        
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part.TextPart(prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = temperature,
                maxOutputTokens = maxTokens,
                candidateCount = 1
            ),
            safetySettings = SafetySetting.childFriendlySettings()
        )
        
        try {
            val response = geminiApi.generateContent(apiKey, request)
            
            // 提取生成的文本
            val candidate = response.candidates.firstOrNull()
                ?: throw IllegalStateException("No response generated")
            
            val textParts = candidate.content.parts.filterIsInstance<Part.TextPart>()
            return@withContext textParts.joinToString("") { it.text }
            
        } catch (e: Exception) {
            // 错误处理和降级策略
            when (e) {
                is retrofit2.HttpException -> {
                    when (e.code()) {
                        429 -> throw RateLimitException("API rate limit exceeded", e)
                        401 -> throw AuthenticationException("Invalid API key", e)
                        else -> throw GenerationException("Failed to generate text: ${e.message()}", e)
                    }
                }
                else -> throw GenerationException("Failed to generate text: ${e.message}", e)
            }
        }
    }
    
    /**
     * 生成对话
     */
    suspend fun generateChat(
        messages: List<ChatTurn>,
        maxTokens: Int = 500,
        temperature: Float = 0.7f
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getGeminiApiKey()
            ?: throw IllegalStateException("Gemini API key not available")
        
        val contents = messages.map { turn ->
            Content(
                role = when (turn.role) {
                    ChatRole.USER -> "user"
                    ChatRole.MODEL -> "model"
                    ChatRole.SYSTEM -> "user" // Gemini doesn't have system role
                },
                parts = listOf(Part.TextPart(turn.message))
            )
        }
        
        val request = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = temperature,
                maxOutputTokens = maxTokens
            ),
            safetySettings = SafetySetting.childFriendlySettings()
        )
        
        val response = geminiApi.generateContent(apiKey, request)
        val candidate = response.candidates.firstOrNull()
            ?: throw IllegalStateException("No response generated")
        
        val textParts = candidate.content.parts.filterIsInstance<Part.TextPart>()
        return@withContext textParts.joinToString("") { it.text }
    }
    
    /**
     * 生成儿童故事
     */
    suspend fun generateChildStory(
        theme: String,
        age: Int,
        length: Int = 500,
        educationalFocus: String? = null
    ): String {
        val prompt = buildString {
            appendLine("请为${age}岁的小朋友创作一个温馨有趣的故事。")
            appendLine("故事主题：$theme")
            appendLine("要求：")
            appendLine("1. 使用简单易懂的语言，适合${age}岁儿童理解")
            appendLine("2. 内容积极向上，传递正能量")
            appendLine("3. 包含有趣的角色和情节")
            appendLine("4. 故事要有教育意义但不说教")
            educationalFocus?.let {
                appendLine("5. 融入以下教育重点：$it")
            }
            appendLine("6. 长度适中，朗读时间约${length / 100}分钟")
            appendLine("\n请开始创作这个精彩的故事：")
        }
        
        return generateText(prompt, length, 0.8f)
    }
}
/**
 * 对话角色
 */
enum class ChatRole {
    USER,
    MODEL,
    SYSTEM
}
/**
 * 对话轮次
 */
data class ChatTurn(
    val role: ChatRole,
    val message: String
)
/**
 * 自定义异常
 */
class GenerationException(message: String, cause: Throwable? = null) : Exception(message, cause)
class RateLimitException(message: String, cause: Throwable? = null) : Exception(message, cause)
class AuthenticationException(message: String, cause: Throwable? = null) : Exception(message, cause)
