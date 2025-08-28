package com.enlightenment.ai.model

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi


/**
 * AI模型接口
 */
interface AIModel {
    /**
     * 模型名称
     */
    val name: String
    
    /**
     * 模型版本
     */
    val version: String
    
    /**
     * 模型是否准备就绪
     */
    suspend fun isReady(): Boolean
    
    /**
     * 初始化模型
     */
    suspend fun initialize()
    
    /**
     * 释放模型资源
     */
    suspend fun release()
}
/**
 * 文本生成模型
 */
interface TextGenerationModel : AIModel {
    /**
     * 生成文本
     * @param prompt 提示词
     * @param maxTokens 最大token数
     * @param temperature 温度参数(0-1)，控制生成的随机性
     * @return 生成的文本
     */
    suspend fun generateText(
        prompt: String,
        maxTokens: Int = 500,
        temperature: Float = 0.7f
    ): String
}
/**
 * 图像识别模型
 */
interface ImageRecognitionModel : AIModel {
    /**
     * 识别图像中的物体
     * @param imageData 图像数据
     * @return 识别结果列表
     */
    suspend fun recognizeImage(imageData: ByteArray): List<RecognitionResult>
}
/**
 * 语音识别模型
 */
interface SpeechRecognitionModel : AIModel {
    /**
     * 识别语音
     * @param audioData 音频数据
     * @return 识别的文本
     */
    suspend fun recognizeSpeech(audioData: ByteArray): String
}
/**
 * 语音合成模型
 */
interface TextToSpeechModel : AIModel {
    /**
     * 文本转语音
     * @param text 要转换的文本
     * @param voice 语音类型
     * @return 音频数据
     */
    suspend fun synthesizeSpeech(
        text: String,
        voice: VoiceType = VoiceType.CHILD_FRIENDLY
    ): ByteArray
}
/**
 * 识别结果
 */
data class RecognitionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox? = null
)
/**
 * 边界框
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
/**
 * 语音类型
 */
enum class VoiceType {
    CHILD_FRIENDLY,  // 儿童友好语音
    MALE_ADULT,      // 成年男性语音
    FEMALE_ADULT     // 成年女性语音
}
