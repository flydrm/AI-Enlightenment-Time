package com.enlightenment.data.network.api

import retrofit2.http.*

/**
 * Google Gemini API接口
 * 文档：https://ai.google.dev/tutorials/rest_quickstart
 */
interface GeminiApi {
    
    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val API_VERSION = "v1beta"
        const val MODEL_NAME = "gemini-2.5-pro"
    }
    
    /**
     * 生成内容
     */
    @POST("$API_VERSION/models/$MODEL_NAME:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
    
    /**
     * 流式生成内容
     */
    @POST("$API_VERSION/models/$MODEL_NAME:streamGenerateContent")
    @Streaming
    suspend fun streamGenerateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): okhttp3.ResponseBody
    
    /**
     * 计算token数量
     */
    @POST("$API_VERSION/models/$MODEL_NAME:countTokens")
    suspend fun countTokens(
        @Query("key") apiKey: String,
        @Body request: TokenCountRequest
    ): TokenCountResponse
}

/**
 * Gemini请求体
 */
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null
)

/**
 * 内容
 */
data class Content(
    val role: String, // "user" or "model"
    val parts: List<Part>
)

/**
 * 内容部分
 */
sealed class Part {
    data class TextPart(val text: String) : Part()
    data class InlineDataPart(val inlineData: InlineData) : Part()
}

/**
 * 内联数据（图片等）
 */
data class InlineData(
    val mimeType: String,
    val data: String // Base64编码
)

/**
 * 生成配置
 */
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val candidateCount: Int? = null,
    val maxOutputTokens: Int? = null,
    val stopSequences: List<String>? = null
)

/**
 * 安全设置
 */
data class SafetySetting(
    val category: String,
    val threshold: String
) {
    companion object {
        // 安全类别
        const val HARM_CATEGORY_HARASSMENT = "HARM_CATEGORY_HARASSMENT"
        const val HARM_CATEGORY_HATE_SPEECH = "HARM_CATEGORY_HATE_SPEECH"
        const val HARM_CATEGORY_SEXUALLY_EXPLICIT = "HARM_CATEGORY_SEXUALLY_EXPLICIT"
        const val HARM_CATEGORY_DANGEROUS_CONTENT = "HARM_CATEGORY_DANGEROUS_CONTENT"
        
        // 阈值
        const val BLOCK_NONE = "BLOCK_NONE"
        const val BLOCK_ONLY_HIGH = "BLOCK_ONLY_HIGH"
        const val BLOCK_MEDIUM_AND_ABOVE = "BLOCK_MEDIUM_AND_ABOVE"
        const val BLOCK_LOW_AND_ABOVE = "BLOCK_LOW_AND_ABOVE"
        
        // 儿童友好的安全设置
        fun childFriendlySettings() = listOf(
            SafetySetting(HARM_CATEGORY_HARASSMENT, BLOCK_LOW_AND_ABOVE),
            SafetySetting(HARM_CATEGORY_HATE_SPEECH, BLOCK_LOW_AND_ABOVE),
            SafetySetting(HARM_CATEGORY_SEXUALLY_EXPLICIT, BLOCK_LOW_AND_ABOVE),
            SafetySetting(HARM_CATEGORY_DANGEROUS_CONTENT, BLOCK_LOW_AND_ABOVE)
        )
    }
}

/**
 * Gemini响应
 */
data class GeminiResponse(
    val candidates: List<Candidate>,
    val promptFeedback: PromptFeedback? = null
)

/**
 * 候选结果
 */
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val index: Int,
    val safetyRatings: List<SafetyRating>? = null
)

/**
 * 安全评级
 */
data class SafetyRating(
    val category: String,
    val probability: String
)

/**
 * 提示反馈
 */
data class PromptFeedback(
    val safetyRatings: List<SafetyRating>
)

/**
 * Token计数请求
 */
data class TokenCountRequest(
    val contents: List<Content>
)

/**
 * Token计数响应
 */
data class TokenCountResponse(
    val totalTokens: Int
)