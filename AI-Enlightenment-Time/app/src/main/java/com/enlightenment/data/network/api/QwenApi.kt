package com.enlightenment.data.network.api

import retrofit2.http.*



/**
 * 通义千问嵌入模型API接口
 * 用于图像向量化
 */
interface QwenApi {
    
    companion object {
        const val BASE_URL = "https://dashscope.aliyuncs.com/"
        const val API_VERSION = "api/v1"
        const val EMBEDDING_MODEL = "qwen3-embedding-8b"
        const val MULTIMODAL_MODEL = "qwen-vl-plus"
    }
    
    /**
     * 创建文本嵌入
     */
    @POST("$API_VERSION/services/embeddings/text-embedding/text-embedding")
    suspend fun createTextEmbedding(
        @Header("Authorization") authorization: String,
        @Body request: QwenEmbeddingRequest
    ): QwenEmbeddingResponse
    
    /**
     * 创建多模态嵌入（图像+文本）
     */
    @POST("$API_VERSION/services/aigc/multimodal-generation/generation")
    suspend fun createMultimodalEmbedding(
        @Header("Authorization") authorization: String,
        @Body request: QwenMultimodalRequest
    ): QwenMultimodalResponse
    
    /**
     * 图像理解
     */
    @POST("$API_VERSION/services/aigc/multimodal-generation/generation")
    suspend fun understandImage(
        @Header("Authorization") authorization: String,
        @Body request: QwenImageUnderstandingRequest
    ): QwenImageUnderstandingResponse
}
/**
 * 嵌入请求
 */
data class QwenEmbeddingRequest(
    val model: String = QwenApi.EMBEDDING_MODEL,
    val input: QwenInput,
    val parameters: QwenEmbeddingParameters? = null
)
/**
 * 输入数据
 */
data class QwenInput(
    val texts: List<String>
)
/**
 * 嵌入参数
 */
data class QwenEmbeddingParameters(
    val textType: String? = "query" // query, document
)
/**
 * 嵌入响应
 */
data class QwenEmbeddingResponse(
    val output: QwenEmbeddingOutput,
    val usage: QwenUsage,
    val requestId: String
)
/**
 * 嵌入输出
 */
data class QwenEmbeddingOutput(
    val embeddings: List<Embedding>
)
/**
 * 嵌入向量
 */
data class Embedding(
    val textIndex: Int,
    val embedding: List<Float>
)
/**
 * 多模态请求
 */
data class QwenMultimodalRequest(
    val model: String = QwenApi.MULTIMODAL_MODEL,
    val input: QwenMultimodalInput,
    val parameters: QwenMultimodalParameters? = null
)
/**
 * 多模态输入
 */
data class QwenMultimodalInput(
    val messages: List<QwenMessage>
)
/**
 * 消息
 */
data class QwenMessage(
    val role: String, // system, user, assistant
    val content: List<QwenContent>
)
/**
 * 内容
 */
sealed class QwenContent {
    data class TextContent(val text: String) : QwenContent()
    data class ImageContent(val image: String) : QwenContent() // base64 或 URL
}
/**
 * 多模态参数
 */
data class QwenMultimodalParameters(
    val seedId: Int? = null,
    val maxLength: Int? = null
)
/**
 * 多模态响应
 */
data class QwenMultimodalResponse(
    val output: QwenMultimodalOutput,
    val usage: QwenUsage,
    val requestId: String
)
/**
 * 多模态输出
 */
data class QwenMultimodalOutput(
    val choices: List<QwenChoice>
)
/**
 * 选择
 */
data class QwenChoice(
    val finishReason: String,
    val message: QwenMessage
)
/**
 * 使用统计
 */
data class QwenUsage(
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val imageCount: Int? = null
)
/**
 * 图像理解请求
 */
data class QwenImageUnderstandingRequest(
    val model: String = QwenApi.MULTIMODAL_MODEL,
    val input: QwenMultimodalInput,
    val parameters: QwenImageParameters? = null
) {
    companion object {
        fun createChildFriendlyRequest(imageBase64: String, childAge: Int) = 
            QwenImageUnderstandingRequest(
                input = QwenMultimodalInput(
                    messages = listOf(
                        QwenMessage(
                            role = "system",
                            content = listOf(
                                QwenContent.TextContent(
                                    "你是一个友善的AI助手，专门为${childAge}岁的孩子解释图片内容。" +
                                    "请用简单、有趣、安全的语言描述图片，激发孩子的好奇心和想象力。"
                                )
                            )
                        ),
                        QwenMessage(
                            role = "user",
                            content = listOf(
                                QwenContent.ImageContent(imageBase64),
                                QwenContent.TextContent("这张图片里有什么有趣的东西呢？")
                            )
                        )
                    )
                )
            )
    }
}
/**
 * 图像参数
 */
data class QwenImageParameters(
    val maxLength: Int? = 1500,
    val temperature: Float? = 0.8f
)
/**
 * 图像理解响应
 */
typealias QwenImageUnderstandingResponse = QwenMultimodalResponse
