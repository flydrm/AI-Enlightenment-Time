package com.enlightenment.ai.model.impl

import com.enlightenment.ai.model.TextToSpeechModel
import com.enlightenment.ai.model.VoiceType
import com.enlightenment.data.network.api.OpenAIApi
import com.enlightenment.data.network.api.SpeechRequest
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OpenAI文本转语音模型实现
 * 使用高质量的TTS模型生成自然的语音
 */
@Singleton
class OpenAITextToSpeechModel @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val secureStorage: SecureStorage
) : TextToSpeechModel {
    
    override val name = "OpenAI TTS"
    override val version = "tts-1-hd"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        val apiKey = secureStorage.getOpenAIApiKey()
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("OpenAI API key not configured")
        }
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun synthesizeSpeech(
        text: String,
        voice: VoiceType
    ): ByteArray = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getOpenAIApiKey()
            ?: throw IllegalStateException("OpenAI API key not available")
        
        // 选择合适的语音
        val voiceName = selectVoice(voice)
        
        // 调整语速
        val speed = when (voice) {
            VoiceType.CHILD_FRIENDLY -> 0.9f // 稍慢一点，让儿童更容易理解
            else -> 1.0f
        }
        
        val request = SpeechRequest(
            model = "tts-1-hd", // 使用高质量模型
            input = preprocessText(text, voice),
            voice = voiceName,
            responseFormat = "mp3",
            speed = speed
        )
        
        try {
            val response = openAIApi.createSpeech("Bearer $apiKey", request)
            return@withContext response.bytes()
            
        } catch (e: Exception) {
            when (e) {
                is retrofit2.HttpException -> {
                    when (e.code()) {
                        429 -> throw RateLimitException("API rate limit exceeded", e)
                        401 -> throw AuthenticationException("Invalid API key", e)
                        400 -> throw InvalidTextException("Invalid text for speech synthesis", e)
                        else -> throw SynthesisException("Failed to synthesize speech: ${e.message()}", e)
                    }
                }
                else -> throw SynthesisException("Failed to synthesize speech: ${e.message}", e)
            }
        }
    }
    
    /**
     * 生成故事朗读语音
     */
    suspend fun synthesizeStoryNarration(
        storyText: String,
        characterVoices: Map<String, String> = emptyMap()
    ): ByteArray = withContext(Dispatchers.IO) {
        // 对于简单实现，使用单一语音朗读整个故事
        // 未来可以扩展为根据角色使用不同语音
        val processedText = preprocessStoryText(storyText)
        return@withContext synthesizeSpeech(processedText, VoiceType.CHILD_FRIENDLY)
    }
    
    /**
     * 生成交互式语音
     */
    suspend fun synthesizeInteractiveSpeech(
        text: String,
        emotion: Emotion = Emotion.HAPPY
    ): ByteArray = withContext(Dispatchers.IO) {
        val emotionalText = addEmotionalCues(text, emotion)
        return@withContext synthesizeSpeech(emotionalText, VoiceType.CHILD_FRIENDLY)
    }
    
    /**
     * 批量生成语音
     */
    suspend fun batchSynthesize(
        texts: List<String>,
        voice: VoiceType = VoiceType.CHILD_FRIENDLY
    ): List<ByteArray> = withContext(Dispatchers.IO) {
        texts.map { text ->
            synthesizeSpeech(text, voice)
        }
    }
    
    /**
     * 选择合适的语音
     */
    private fun selectVoice(voiceType: VoiceType): String {
        return when (voiceType) {
            VoiceType.CHILD_FRIENDLY -> "nova" // Nova - 温暖友好的女声，适合儿童
            VoiceType.MALE_ADULT -> "onyx" // Onyx - 深沉的男声
            VoiceType.FEMALE_ADULT -> "alloy" // Alloy - 中性的女声
        }
    }
    
    /**
     * 预处理文本
     */
    private fun preprocessText(text: String, voiceType: VoiceType): String {
        var processed = text
        
        // 清理文本
        processed = processed.trim()
            .replace("\\s+".toRegex(), " ") // 多个空格替换为单个
            .replace("。。。", "…") // 省略号规范化
        
        // 为儿童语音添加适当的停顿
        if (voiceType == VoiceType.CHILD_FRIENDLY) {
            // 在句号后添加停顿
            processed = processed.replace("。", "。 ")
            // 在问号后添加停顿
            processed = processed.replace("？", "？ ")
            // 在感叹号后添加停顿
            processed = processed.replace("！", "！ ")
        }
        
        // 限制文本长度（API限制）
        if (processed.length > 4096) {
            processed = processed.take(4090) + "..."
        }
        
        return processed
    }
    
    /**
     * 预处理故事文本
     */
    private fun preprocessStoryText(text: String): String {
        var processed = text
        
        // 添加故事开始提示
        if (!processed.startsWith("从前") && !processed.startsWith("很久")) {
            processed = "让我给你讲一个精彩的故事。 $processed"
        }
        
        // 添加故事结束提示
        if (!processed.endsWith("。") && !processed.endsWith("！")) {
            processed = "$processed。"
        }
        
        // 为对话添加语气词
        processed = processed
            .replace(""", ""哦，")
            .replace("："", "说："")
        
        return preprocessText(processed, VoiceType.CHILD_FRIENDLY)
    }
    
    /**
     * 添加情感线索
     */
    private fun addEmotionalCues(text: String, emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "（开心地）$text"
            Emotion.EXCITED -> "（兴奋地）$text！"
            Emotion.CURIOUS -> "（好奇地）$text？"
            Emotion.GENTLE -> "（温柔地）$text"
            Emotion.ENCOURAGING -> "（鼓励地）$text，你真棒！"
            Emotion.SURPRISED -> "（惊喜地）哇！$text"
        }
    }
    
    /**
     * 验证文本是否适合转换
     */
    private fun validateText(text: String): Boolean {
        // 检查文本长度
        if (text.isEmpty() || text.length > 4096) {
            return false
        }
        
        // 检查是否包含不支持的字符
        // OpenAI TTS支持大多数Unicode字符，这里只做基本检查
        return true
    }
}

/**
 * 情感枚举
 */
enum class Emotion {
    HAPPY,        // 快乐
    EXCITED,      // 兴奋
    CURIOUS,      // 好奇
    GENTLE,       // 温柔
    ENCOURAGING,  // 鼓励
    SURPRISED     // 惊喜
}

/**
 * 自定义异常
 */
class SynthesisException(message: String, cause: Throwable? = null) : Exception(message, cause)
class InvalidTextException(message: String, cause: Throwable? = null) : Exception(message, cause)