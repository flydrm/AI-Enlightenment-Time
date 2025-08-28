package com.enlightenment.ai.model.impl

import com.enlightenment.ai.model.SpeechRecognitionModel
import com.enlightenment.data.network.api.OpenAIApi
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * OpenAI语音识别模型实现
 * 使用Whisper模型进行高质量的语音转文本
 */

class OpenAISpeechRecognitionModel constructor(
    private val openAIApi: OpenAIApi,
    private val secureStorage: SecureStorage
) : SpeechRecognitionModel {
    
    override val name = "Whisper"
    override val version = "large-v3"
    
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
    
    override suspend fun recognizeSpeech(audioData: ByteArray): String = 
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getOpenAIApiKey()
                ?: throw IllegalStateException("OpenAI API key not available")
            
            // 创建临时文件保存音频数据
            val tempFile = File.createTempFile("audio_", ".mp3")
            try {
                // 写入音频数据到临时文件
                FileOutputStream(tempFile).use { fos ->
                    fos.write(audioData)
                }
                
                // 创建多部分请求体
                val requestFile = tempFile.readBytes().toRequestBody("audio/mpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    tempFile.name,
                    requestFile
                )
                
                // 调用API
                val response = openAIApi.createTranscription(
                    authorization = "Bearer $apiKey",
                    model = "whisper-1",
                    file = filePart,
                    language = "zh", // 中文
                    prompt = "这是一个儿童的语音输入。", // 提示以提高准确性
                    temperature = 0.2f // 较低的温度以提高准确性
                )
                
                return@withContext response.text.trim()
                
            } catch (e: Exception) {
                when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            429 -> throw RateLimitException("API rate limit exceeded", e)
                            401 -> throw AuthenticationException("Invalid API key", e)
                            413 -> throw AudioTooLargeException("Audio file too large", e)
                            else -> throw RecognitionException("Failed to recognize speech: ${e.message()}", e)
                        }
                    }
                    else -> throw RecognitionException("Failed to recognize speech: ${e.message}", e)
                }
            } finally {
                // 清理临时文件
                tempFile.delete()
            }
        }
    
    /**
     * 识别儿童语音（针对儿童语音特点优化）
     */
    suspend fun recognizeChildSpeech(
        audioData: ByteArray,
        childAge: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getOpenAIApiKey()
            ?: throw IllegalStateException("OpenAI API key not available")
        
        val tempFile = File.createTempFile("child_audio_", ".mp3")
        try {
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioData)
            }
            
            val requestFile = tempFile.readBytes().toRequestBody("audio/mpeg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                tempFile.name,
                requestFile
            )
            
            // 针对儿童语音的特殊提示
            val childPrompt = buildChildPrompt(childAge)
            
            val response = openAIApi.createTranscription(
                authorization = "Bearer $apiKey",
                model = "whisper-1",
                file = filePart,
                language = "zh",
                prompt = childPrompt,
                temperature = 0.3f // 稍高一点的温度以适应儿童发音的多样性
            )
            
            // 后处理：纠正常见的儿童发音错误
            return@withContext postProcessChildSpeech(response.text.trim(), childAge)
            
        } finally {
            tempFile.delete()
        }
    }
    
    /**
     * 实时语音识别（流式处理）
     */
    suspend fun recognizeSpeechStream(
        audioChunks: List<ByteArray>
    ): List<String> = withContext(Dispatchers.IO) {
        // 将音频块合并
        val fullAudio = audioChunks.fold(ByteArray(0)) { acc, chunk ->
            acc + chunk
        }
        
        // 如果音频太短，返回空
        if (fullAudio.size < 1024) { // 小于1KB
            return@withContext emptyList()
        }
        
        // 使用标准识别
        val result = recognizeSpeech(fullAudio)
        
        // 分句返回
        return@withContext result.split("。", "！", "？", "，")
            .filter { it.isNotBlank() }
            .map { it.trim() }
    }
    
    /**
     * 构建儿童语音提示
     */
    private fun buildChildPrompt(age: Int): String {
        return when (age) {
            3, 4 -> "这是一个${age}岁小朋友的语音。可能包含童言童语，发音不太清晰。常见词汇：妈妈、爸爸、玩、吃、睡觉、故事、动物。"
            5, 6 -> "这是一个${age}岁小朋友的语音。发音相对清晰，可能会说一些简单的句子。常见话题：游戏、朋友、学校、动画片。"
            else -> "这是一个儿童的语音输入，请注意儿童特有的发音特点。"
        }
    }
    
    /**
     * 后处理儿童语音识别结果
     */
    private fun postProcessChildSpeech(text: String, age: Int): String {
        var processed = text
        
        // 常见的儿童发音替换
        val replacements = mapOf(
            "肚肚" to "肚子",
            "怕怕" to "害怕",
            "车车" to "汽车",
            "狗狗" to "小狗",
            "猫猫" to "小猫",
            "饭饭" to "吃饭",
            "觉觉" to "睡觉",
            "妈咪" to "妈妈",
            "爹地" to "爸爸",
            "白白" to "拜拜"
        )
        
        // 应用替换
        replacements.forEach { (childWord, correctWord) ->
            processed = processed.replace(childWord, correctWord)
        }
        
        // 修正语法（简单处理）
        if (age <= 4) {
            // 3-4岁儿童可能省略"我"
            if (!processed.startsWith("我") && 
                (processed.startsWith("要") || processed.startsWith("想") || processed.startsWith("喜欢"))) {
                processed = "我$processed"
            }
        }
        
        return processed
    }
    
    /**
     * 检查音频格式和质量
     */
    private fun validateAudio(audioData: ByteArray): Boolean {
        // 检查音频大小（Whisper API限制为25MB）
        if (audioData.size > 25 * 1024 * 1024) {
            return false
        }
        
        // 检查音频长度（太短的音频可能无法识别）
        if (audioData.size < 1024) { // 小于1KB
            return false
        }
        
        return true
    }
}

/**
 * 自定义异常
 */
class RecognitionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class AudioTooLargeException(message: String, cause: Throwable? = null) : Exception(message, cause)