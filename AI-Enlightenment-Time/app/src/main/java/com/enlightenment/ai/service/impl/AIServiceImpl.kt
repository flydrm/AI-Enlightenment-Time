package com.enlightenment.ai.service.impl

import com.enlightenment.ai.service.*
import com.enlightenment.ai.model.*
import com.enlightenment.ai.config.AIConfigManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI服务实现
 */
@Singleton
class AIServiceImpl @Inject constructor(
    private val textGenerationModel: TextGenerationModel,
    private val imageRecognitionModel: ImageRecognitionModel,
    private val speechRecognitionModel: SpeechRecognitionModel,
    private val textToSpeechModel: TextToSpeechModel,
    private val configManager: AIConfigManager
) : AIService {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    override val storyGenerator: StoryGenerationService by lazy {
        StoryGenerationServiceImpl(textGenerationModel, imageGenerationService)
    }
    override val imageRecognition: ImageRecognitionService = ImageRecognitionServiceImpl(
        imageRecognitionModel, 
        textGenerationModel
    )
    override val speechService: SpeechService = SpeechServiceImpl(
        speechRecognitionModel,
        textToSpeechModel
    )
    override val imageGenerationService: ImageGenerationService = ImageGenerationServiceImpl(configManager)
    
    override suspend fun initialize() {
        try {
            // 初始化所有模型
            textGenerationModel.initialize()
            imageRecognitionModel.initialize()
            speechRecognitionModel.initialize()
            textToSpeechModel.initialize()
            
            // 检查所有模型是否准备就绪
            _isReady.value = textGenerationModel.isReady() &&
                    imageRecognitionModel.isReady() &&
                    speechRecognitionModel.isReady() &&
                    textToSpeechModel.isReady()
        } catch (e: Exception) {
            _isReady.value = false
            throw e
        }
    }
    
    override suspend fun release() {
        _isReady.value = false
        textGenerationModel.release()
        imageRecognitionModel.release()
        speechRecognitionModel.release()
        textToSpeechModel.release()
    }
}

/**
 * 故事生成服务实现
 */
class StoryGenerationServiceImpl(
    private val textGenerationModel: TextGenerationModel,
    private val imageGenerationService: ImageGenerationService? = null
) : StoryGenerationService {
    
    override suspend fun generateStory(
        theme: String,
        age: Int,
        preferences: StoryPreferences
    ): Story {
        val prompt = buildStoryPrompt(theme, age, preferences)
        val storyContent = textGenerationModel.generateText(
            prompt = prompt,
            maxTokens = when (preferences.length) {
                StoryLength.SHORT -> 300
                StoryLength.MEDIUM -> 500
                StoryLength.LONG -> 800
            },
            temperature = 0.8f
        )
        
        val title = extractTitle(storyContent)
        
        // 生成配图
        val imageUrl = imageGenerationService?.generateStoryImage(
            storyTitle = title,
            storyContent = storyContent.take(500), // 使用前500字作为内容摘要
            style = ImageStyle.CHILDREN_BOOK
        )
        
        return Story(
            id = generateStoryId(),
            title = title,
            content = storyContent,
            chapters = parseChapters(storyContent),
            imageUrl = imageUrl
        )
    }
    
    override suspend fun continueStory(storyId: String, userChoice: String): StoryChapter {
        val prompt = "继续这个故事，基于用户的选择：$userChoice"
        val continuation = textGenerationModel.generateText(
            prompt = prompt,
            maxTokens = 200,
            temperature = 0.8f
        )
        
        return StoryChapter(
            id = generateChapterId(),
            content = continuation,
            choices = extractChoices(continuation)
        )
    }
    
    private fun buildStoryPrompt(theme: String, age: Int, preferences: StoryPreferences): String {
        return buildString {
            append("为${age}岁的儿童创作一个")
            append(preferences.genre.toChinese())
            append("故事，主题是：$theme。")
            
            if (preferences.includeEducationalContent) {
                append("请包含一些教育内容，让孩子在听故事的同时学到知识。")
            }
            
            if (preferences.characterNames.isNotEmpty()) {
                append("故事中的角色包括：${preferences.characterNames.joinToString("、")}。")
            }
            
            append("故事应该温馨有趣，适合儿童，长度为")
            append(preferences.length.toChinese())
            append("。")
            
            append("请用简单易懂的语言，避免复杂的词汇。")
        }
    }
    
    private fun StoryGenre.toChinese(): String = when (this) {
        StoryGenre.ADVENTURE -> "冒险"
        StoryGenre.FANTASY -> "奇幻"
        StoryGenre.SCIENCE -> "科学"
        StoryGenre.FRIENDSHIP -> "友谊"
        StoryGenre.ANIMAL -> "动物"
        StoryGenre.FAIRY_TALE -> "童话"
    }
    
    private fun StoryLength.toChinese(): String = when (this) {
        StoryLength.SHORT -> "3-5分钟"
        StoryLength.MEDIUM -> "5-10分钟"
        StoryLength.LONG -> "10-15分钟"
    }
    
    private fun extractTitle(content: String): String {
        // 简单实现：取第一行或前20个字符作为标题
        return content.lines().firstOrNull()?.take(20) ?: "精彩故事"
    }
    
    private fun parseChapters(content: String): List<StoryChapter> {
        // 简单实现：将内容分段作为章节
        val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
        return paragraphs.mapIndexed { index, paragraph ->
            StoryChapter(
                id = "chapter_$index",
                content = paragraph,
                choices = if (index < paragraphs.size - 1) {
                    listOf("继续听故事", "选择另一个结局")
                } else {
                    emptyList()
                }
            )
        }
    }
    
    private fun extractChoices(content: String): List<String> {
        // 简单实现：返回默认选项
        return listOf("继续冒险", "返回家园", "寻找朋友")
    }
    
    private fun generateStoryId(): String = "story_${System.currentTimeMillis()}"
    private fun generateChapterId(): String = "chapter_${System.currentTimeMillis()}"
}

/**
 * 图像识别服务实现
 */
class ImageRecognitionServiceImpl(
    private val imageRecognitionModel: ImageRecognitionModel,
    private val textGenerationModel: TextGenerationModel
) : ImageRecognitionService {
    
    override suspend fun recognizeObjects(imageData: ByteArray): List<RecognitionResult> {
        return imageRecognitionModel.recognizeImage(imageData)
    }
    
    override suspend fun generateChildFriendlyDescription(
        recognitionResults: List<RecognitionResult>,
        childAge: Int
    ): String {
        if (recognitionResults.isEmpty()) {
            return "我没有看到什么特别的东西呢，再试试看？"
        }
        
        val topResults = recognitionResults.sortedByDescending { it.confidence }.take(3)
        val prompt = buildDescriptionPrompt(topResults, childAge)
        
        return textGenerationModel.generateText(
            prompt = prompt,
            maxTokens = 150,
            temperature = 0.7f
        )
    }
    
    private fun buildDescriptionPrompt(results: List<RecognitionResult>, age: Int): String {
        return buildString {
            append("为${age}岁的孩子用有趣的方式描述这些物体：")
            results.forEach { result ->
                append("${result.label}（置信度：${(result.confidence * 100).toInt()}%）")
            }
            append("。请用简单的语言，加入一些有趣的知识或故事。")
        }
    }
}

/**
 * 语音服务实现
 */
class SpeechServiceImpl(
    private val speechRecognitionModel: SpeechRecognitionModel,
    private val textToSpeechModel: TextToSpeechModel
) : SpeechService {
    
    private var recognitionJob: MutableSharedFlow<String>? = null
    
    override suspend fun speechToText(audioData: ByteArray): String {
        return speechRecognitionModel.recognizeSpeech(audioData)
    }
    
    override suspend fun textToSpeech(text: String, voice: VoiceType): ByteArray {
        return textToSpeechModel.synthesizeSpeech(text, voice)
    }
    
    override fun startRealtimeSpeechRecognition(): Flow<String> {
        val flow = MutableSharedFlow<String>(
            replay = 0,
            extraBufferCapacity = 10
        )
        recognitionJob = flow
        
        // 启动实时语音识别协程
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 模拟实时语音识别
                // 在实际实现中，这里应该：
                // 1. 使用 AudioRecord 采集音频数据
                // 2. 将音频数据发送到语音识别模型
                // 3. 将识别结果发送到 flow
                
                // 暂时使用模拟数据
                val simulatedPhrases = listOf(
                    "你好，小熊猫",
                    "我想听故事",
                    "这是什么？",
                    "真有趣！"
                )
                
                for (phrase in simulatedPhrases) {
                    delay(2000) // 模拟语音输入间隔
                    flow.emit(phrase)
                }
            } catch (e: Exception) {
                // 错误处理
                flow.emit("语音识别出错了，请重试")
            }
        }
        
        return flow.asSharedFlow()
    }
    
    override fun stopRealtimeSpeechRecognition() {
        recognitionJob = null
    }
}