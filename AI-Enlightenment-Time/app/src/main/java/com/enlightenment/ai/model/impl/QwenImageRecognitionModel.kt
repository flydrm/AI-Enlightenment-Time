package com.enlightenment.ai.model.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.enlightenment.ai.model.BoundingBox
import com.enlightenment.ai.model.ImageRecognitionModel
import com.enlightenment.ai.model.RecognitionResult
import com.enlightenment.data.network.api.*
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * 通义千问图像识别模型实现
 * 使用Qwen3-Embedding-8B进行图像向量化和理解
 */

class QwenImageRecognitionModel constructor(
    private val qwenApi: QwenApi,
    private val secureStorage: SecureStorage
) : ImageRecognitionModel {
    
    override val name = "Qwen3-Embedding-8B"
    override val version = "3.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        val apiKey = secureStorage.getQwenApiKey()
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("Qwen API key not configured")
        }
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun recognizeImage(imageData: ByteArray): List<RecognitionResult> = 
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getQwenApiKey()
                ?: throw IllegalStateException("Qwen API key not available")
            
            // 将图像数据转换为Base64
            val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)
            
            // 创建图像理解请求
            val request = QwenImageUnderstandingRequest(
                input = QwenMultimodalInput(
                    messages = listOf(
                        QwenMessage(
                            role = "user",
                            content = listOf(
                                QwenContent.ImageContent("data:image/jpeg;base64,$base64Image"),
                                QwenContent.TextContent(
                                    "请识别这张图片中的主要物体，并给出它们的名称、位置和置信度。" +
                                    "输出格式：物体名称|置信度|位置描述"
                                )
                            )
                        )
                    )
                ),
                parameters = QwenImageParameters(
                    maxLength = 500,
                    temperature = 0.3f // 较低的温度以获得更准确的识别结果
                )
            )
            
            try {
                val response = qwenApi.understandImage("Bearer $apiKey", request)
                
                // 解析响应并转换为识别结果
                val content = response.output.choices.firstOrNull()?.message?.content
                    ?.filterIsInstance<QwenContent.TextContent>()
                    ?.firstOrNull()?.text ?: ""
                
                return@withContext parseRecognitionResults(content)
                
            } catch (e: Exception) {
                when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            429 -> throw RateLimitException("API rate limit exceeded", e)
                            401 -> throw AuthenticationException("Invalid API key", e)
                            else -> throw RecognitionException("Failed to recognize image: ${e.message()}", e)
                        }
                    }
                    else -> throw RecognitionException("Failed to recognize image: ${e.message}", e)
                }
            }
        }
    
    /**
     * 创建图像嵌入向量
     */
    suspend fun createImageEmbedding(imageData: ByteArray): List<Float> = 
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getQwenApiKey()
                ?: throw IllegalStateException("Qwen API key not available")
            
            // 将图像转换为文本描述，然后生成嵌入
            val imageDescription = describeImage(imageData)
            
            val request = QwenEmbeddingRequest(
                input = QwenInput(texts = listOf(imageDescription)),
                parameters = QwenEmbeddingParameters(textType = "document")
            )
            
            val response = qwenApi.createTextEmbedding("Bearer $apiKey", request)
            return@withContext response.output.embeddings.first().embedding
        }
    
    /**
     * 儿童友好的图像识别
     */
    suspend fun recognizeForChild(imageData: ByteArray, childAge: Int): List<RecognitionResult> = 
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getQwenApiKey()
                ?: throw IllegalStateException("Qwen API key not available")
            
            val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)
            
            // 创建适合儿童的识别请求
            val request = QwenImageUnderstandingRequest.createChildFriendlyRequest(base64Image, childAge)
            
            val response = qwenApi.understandImage("Bearer $apiKey", request)
            
            // 解析响应
            val content = response.output.choices.firstOrNull()?.message?.content
                ?.filterIsInstance<QwenContent.TextContent>()
                ?.firstOrNull()?.text ?: ""
            
            // 将友好的描述转换为识别结果
            return@withContext createChildFriendlyResults(content)
        }
    
    /**
     * 描述图像内容
     */
    private suspend fun describeImage(imageData: ByteArray): String {
        val apiKey = secureStorage.getQwenApiKey()
            ?: throw IllegalStateException("Qwen API key not available")
        
        val base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP)
        
        val request = QwenImageUnderstandingRequest(
            input = QwenMultimodalInput(
                messages = listOf(
                    QwenMessage(
                        role = "user",
                        content = listOf(
                            QwenContent.ImageContent("data:image/jpeg;base64,$base64Image"),
                            QwenContent.TextContent("请用一句话描述这张图片的主要内容。")
                        )
                    )
                )
            )
        )
        
        val response = qwenApi.understandImage("Bearer $apiKey", request)
        return response.output.choices.firstOrNull()?.message?.content
            ?.filterIsInstance<QwenContent.TextContent>()
            ?.firstOrNull()?.text ?: "图片内容"
    }
    
    /**
     * 解析识别结果
     */
    private fun parseRecognitionResults(content: String): List<RecognitionResult> {
        return content.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                try {
                    val parts = line.split("|")
                    if (parts.size >= 2) {
                        RecognitionResult(
                            label = parts[0].trim(),
                            confidence = parts.getOrNull(1)?.toFloatOrNull() ?: 0.8f,
                            boundingBox = parseBoundingBox(parts.getOrNull(2))
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    /**
     * 解析边界框
     */
    private fun parseBoundingBox(description: String?): BoundingBox? {
        // 简单实现，实际应该解析更详细的位置信息
        return when {
            description?.contains("左上") == true -> BoundingBox(0f, 0f, 0.5f, 0.5f)
            description?.contains("右上") == true -> BoundingBox(0.5f, 0f, 1f, 0.5f)
            description?.contains("左下") == true -> BoundingBox(0f, 0.5f, 0.5f, 1f)
            description?.contains("右下") == true -> BoundingBox(0.5f, 0.5f, 1f, 1f)
            description?.contains("中间") == true -> BoundingBox(0.25f, 0.25f, 0.75f, 0.75f)
            else -> null
        }
    }
    
    /**
     * 创建儿童友好的识别结果
     */
    private fun createChildFriendlyResults(description: String): List<RecognitionResult> {
        // 从描述中提取关键物体
        val commonObjects = listOf(
            "小狗" to 0.9f, "小猫" to 0.9f, "花朵" to 0.85f, "树木" to 0.85f,
            "汽车" to 0.88f, "房子" to 0.87f, "太阳" to 0.92f, "云朵" to 0.86f,
            "小鸟" to 0.84f, "蝴蝶" to 0.83f, "球" to 0.89f, "玩具" to 0.88f
        )
        
        return commonObjects
            .filter { (obj, _) -> description.contains(obj) }
            .map { (obj, confidence) ->
                RecognitionResult(
                    label = obj,
                    confidence = confidence,
                    boundingBox = null
                )
            }
            .ifEmpty {
                // 如果没有识别到特定物体，返回通用结果
                listOf(
                    RecognitionResult(
                        label = "有趣的东西",
                        confidence = 0.8f,
                        boundingBox = null
                    )
                )
            }
    }
    
    /**
     * 压缩图像以优化API调用
     */
    private fun compressImage(imageData: ByteArray, maxSize: Int = 1024 * 1024): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        val outputStream = ByteArrayOutputStream()
        
        var quality = 90
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        // 如果图像太大，降低质量
        while (outputStream.toByteArray().size > maxSize && quality > 10) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        
        return outputStream.toByteArray()
    }
}

/**
 * 自定义异常
 */
class RecognitionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class RateLimitException(message: String, cause: Throwable? = null) : Exception(message, cause)
class AuthenticationException(message: String, cause: Throwable? = null) : Exception(message, cause)