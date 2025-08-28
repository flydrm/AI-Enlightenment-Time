package com.enlightenment.data.network.api

import retrofit2.http.*

/**
 * OpenAI API接口（GPT-5-PRO）
 * 文档：https://platform.openai.com/docs/api-reference
 */
interface OpenAIApi {
    
    companion object {
        const val BASE_URL = "https://api.openai.com/"
        const val API_VERSION = "v1"
        const val MODEL_NAME = "gpt-5-pro"
    }
    
    /**
     * 创建聊天完成
     */
    @POST("$API_VERSION/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
    
    /**
     * 创建文本嵌入
     */
    @POST("$API_VERSION/embeddings")
    suspend fun createEmbedding(
        @Header("Authorization") authorization: String,
        @Body request: EmbeddingRequest
    ): EmbeddingResponse
    
    /**
     * 文本转语音
     */
    @POST("$API_VERSION/audio/speech")
    suspend fun createSpeech(
        @Header("Authorization") authorization: String,
        @Body request: SpeechRequest
    ): okhttp3.ResponseBody
    
    /**
     * 语音转文本
     */
    @Multipart
    @POST("$API_VERSION/audio/transcriptions")
    suspend fun createTranscription(
        @Header("Authorization") authorization: String,
        @Part("model") model: String,
        @Part file: okhttp3.MultipartBody.Part,
        @Part("language") language: String? = null,
        @Part("prompt") prompt: String? = null,
        @Part("temperature") temperature: Float? = null
    ): TranscriptionResponse
}

/**
 * 聊天完成请求
 */
data class ChatCompletionRequest(
    val model: String = OpenAIApi.MODEL_NAME,
    val messages: List<ChatMessage>,
    val temperature: Float? = null,
    val topP: Float? = null,
    val n: Int? = null,
    val stream: Boolean? = null,
    val stop: List<String>? = null,
    val maxTokens: Int? = null,
    val presencePenalty: Float? = null,
    val frequencyPenalty: Float? = null,
    val user: String? = null
)

/**
 * 聊天消息
 */
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String,
    val name: String? = null
) {
    companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        
        fun system(content: String) = ChatMessage(ROLE_SYSTEM, content)
        fun user(content: String) = ChatMessage(ROLE_USER, content)
        fun assistant(content: String) = ChatMessage(ROLE_ASSISTANT, content)
    }
}

/**
 * 聊天完成响应
 */
data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage? = null
)

/**
 * 聊天选择
 */
data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    val finishReason: String? = null
)

/**
 * 使用统计
 */
data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

/**
 * 嵌入请求
 */
data class EmbeddingRequest(
    val model: String = "text-embedding-3-large",
    val input: String,
    val user: String? = null
)

/**
 * 嵌入响应
 */
data class EmbeddingResponse(
    val `object`: String,
    val data: List<EmbeddingData>,
    val model: String,
    val usage: Usage
)

/**
 * 嵌入数据
 */
data class EmbeddingData(
    val `object`: String,
    val embedding: List<Float>,
    val index: Int
)

/**
 * 语音请求
 */
data class SpeechRequest(
    val model: String = "tts-1-hd",
    val input: String,
    val voice: String = "alloy", // alloy, echo, fable, onyx, nova, shimmer
    val responseFormat: String? = "mp3", // mp3, opus, aac, flac
    val speed: Float? = 1.0f // 0.25 to 4.0
)

/**
 * 转录响应
 */
data class TranscriptionResponse(
    val text: String
)