package com.enlightenment.ai.service

import com.enlightenment.ai.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * AI服务接口，管理所有AI模型
 */
interface AIService {
    /**
     * 故事生成服务
     */
    val storyGenerator: StoryGenerationService
    
    /**
     * 图像识别服务
     */
    val imageRecognition: ImageRecognitionService
    
    /**
     * 语音服务
     */
    val speechService: SpeechService
    
    /**
     * 初始化所有AI服务
     */
    suspend fun initialize()
    
    /**
     * 释放所有资源
     */
    suspend fun release()
    
    /**
     * 服务是否准备就绪
     */
    val isReady: StateFlow<Boolean>
}

/**
 * 故事生成服务
 */
interface StoryGenerationService {
    /**
     * 生成故事
     * @param theme 故事主题
     * @param age 儿童年龄
     * @param preferences 偏好设置
     * @return 生成的故事
     */
    suspend fun generateStory(
        theme: String,
        age: Int,
        preferences: StoryPreferences = StoryPreferences()
    ): Story
    
    /**
     * 续写故事
     * @param storyId 故事ID
     * @param userChoice 用户选择
     * @return 续写的内容
     */
    suspend fun continueStory(
        storyId: String,
        userChoice: String
    ): StoryChapter
}

/**
 * 图像识别服务
 */
interface ImageRecognitionService {
    /**
     * 识别物体
     * @param imageData 图像数据
     * @return 识别结果
     */
    suspend fun recognizeObjects(imageData: ByteArray): List<RecognitionResult>
    
    /**
     * 生成物体描述
     * @param recognitionResults 识别结果
     * @param childAge 儿童年龄
     * @return 适合儿童的描述文本
     */
    suspend fun generateChildFriendlyDescription(
        recognitionResults: List<RecognitionResult>,
        childAge: Int
    ): String
}

/**
 * 语音服务
 */
interface SpeechService {
    /**
     * 语音转文本
     * @param audioData 音频数据
     * @return 识别的文本
     */
    suspend fun speechToText(audioData: ByteArray): String
    
    /**
     * 文本转语音
     * @param text 文本
     * @param voice 语音类型
     * @return 音频数据
     */
    suspend fun textToSpeech(
        text: String,
        voice: VoiceType = VoiceType.CHILD_FRIENDLY
    ): ByteArray
    
    /**
     * 开始实时语音识别
     * @return 识别结果流
     */
    fun startRealtimeSpeechRecognition(): Flow<String>
    
    /**
     * 停止实时语音识别
     */
    fun stopRealtimeSpeechRecognition()
}

/**
 * 故事对象
 */
data class Story(
    val id: String,
    val title: String,
    val content: String,
    val chapters: List<StoryChapter>,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 故事章节
 */
data class StoryChapter(
    val id: String,
    val content: String,
    val choices: List<String> = emptyList(),
    val imageUrl: String? = null
)

/**
 * 故事偏好设置
 */
data class StoryPreferences(
    val length: StoryLength = StoryLength.MEDIUM,
    val genre: StoryGenre = StoryGenre.ADVENTURE,
    val includeEducationalContent: Boolean = true,
    val characterNames: List<String> = emptyList()
)

/**
 * 故事长度
 */
enum class StoryLength {
    SHORT,   // 3-5分钟
    MEDIUM,  // 5-10分钟
    LONG     // 10-15分钟
}

/**
 * 故事类型
 */
enum class StoryGenre {
    ADVENTURE,    // 冒险
    FANTASY,      // 奇幻
    SCIENCE,      // 科学
    FRIENDSHIP,   // 友谊
    ANIMAL,       // 动物
    FAIRY_TALE    // 童话
}