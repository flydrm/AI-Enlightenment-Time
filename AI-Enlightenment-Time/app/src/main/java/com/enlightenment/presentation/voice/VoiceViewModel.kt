package com.enlightenment.presentation.voice

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.service.AIService
import com.enlightenment.multimedia.audio.AudioService
import com.enlightenment.multimedia.audio.RecordingState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * 语音对话ViewModel
 */

class VoiceViewModel constructor(
    private val context: Context,
    private val audioService: AudioService,
    private val aiService: AIService
) : ViewModel() {
    
    companion object {
        private const val TAG = "VoiceViewModel"
    }
    
    // 语音状态
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()
    
    // 对话记录
    private val _conversation = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversation: StateFlow<List<ConversationMessage>> = _conversation.asStateFlow()
    
    // 是否正在监听
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    // 音频振幅（用于动画）
    val audioAmplitude: StateFlow<Float> = audioService.getAudioAmplitude()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )
    
    private var audioBuffer = ByteArrayOutputStream()
    
    init {
        // 初始化音频服务
        viewModelScope.launch {
            try {
                audioService.initialize(context)
                
                // 添加欢迎消息
                addMessage(
                    ConversationMessage(
                        content = "你好呀！我是小熊猫乐乐，有什么想和我聊的吗？",
                        isUser = false
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize audio service", e)
                _voiceState.value = VoiceState.Error(e.message ?: "初始化失败")
            }
        }
        
        // 监听录音状态
        viewModelScope.launch {
            audioService.recordingState.collect { state ->
                when (state) {
                    is RecordingState.Recording -> {
                        _voiceState.value = VoiceState.Listening
                    }
                    is RecordingState.Processing -> {
                        _voiceState.value = VoiceState.Processing
                    }
                    is RecordingState.Error -> {
                        _voiceState.value = VoiceState.Error(state.message)
                        _isListening.value = false
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * 开始监听
     */
    fun startListening() {
        if (_isListening.value) return
        
        viewModelScope.launch {
            try {
                _isListening.value = true
                _voiceState.value = VoiceState.Listening
                audioBuffer.reset()
                
                // 开始录音
                audioService.startRecording().collect { audioData ->
                    audioBuffer.write(audioData.data)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start listening", e)
                _voiceState.value = VoiceState.Error(e.message ?: "录音失败")
                _isListening.value = false
            }
        }
    }
    
    /**
     * 停止监听
     */
    fun stopListening() {
        if (!_isListening.value) return
        
        viewModelScope.launch {
            try {
                _isListening.value = false
                audioService.stopRecording()
                
                val audioData = audioBuffer.toByteArray()
                if (audioData.isNotEmpty()) {
                    processAudio(audioData)
                } else {
                    _voiceState.value = VoiceState.Idle
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop listening", e)
                _voiceState.value = VoiceState.Error(e.message ?: "处理失败")
            }
        }
    }
    
    /**
     * 处理录音数据
     */
    private suspend fun processAudio(audioData: ByteArray) {
        try {
            _voiceState.value = VoiceState.Processing
            
            // 使用AI服务进行语音识别
            val recognizedText = aiService.speechService.speechToText(audioData)
            
            if (recognizedText.isNotBlank()) {
                // 添加用户消息
                addMessage(
                    ConversationMessage(
                        content = recognizedText,
                        isUser = true
                    )
                )
                
                // 生成AI回复
                val response = generateResponse(recognizedText)
                
                // 添加AI消息
                addMessage(
                    ConversationMessage(
                        content = response,
                        isUser = false
                    )
                )
                
                // 语音合成并播放
                _voiceState.value = VoiceState.Speaking
                val speechAudio = aiService.speechService.textToSpeech(response)
                audioService.playAudio(speechAudio)
            }
            
            _voiceState.value = VoiceState.Idle
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process audio", e)
            _voiceState.value = VoiceState.Error(e.message ?: "处理失败")
        }
    }
    
    /**
     * 生成AI回复
     */
    private suspend fun generateResponse(userInput: String): String {
        // 构建对话上下文
        val context = buildConversationContext()
        val prompt = """
            你是小熊猫乐乐，一个友善、活泼的AI助手，专门陪伴3-6岁的小朋友。
            请用简单、温暖、充满童趣的语言回复。
            
            之前的对话：
            $context
            
            小朋友说：$userInput
            
            请回复（保持简短，1-2句话）：
        """.trimIndent()
        
        return aiService.storyGenerator.generateStory(
            theme = prompt,
            age = 5,
            preferences = com.enlightenment.ai.service.StoryPreferences()
        ).content.take(100) // 简单处理，取前100个字符
    }
    
    /**
     * 构建对话上下文
     */
    private fun buildConversationContext(): String {
        return _conversation.value
            .takeLast(6) // 最近3轮对话
            .joinToString("\n") { message ->
                if (message.isUser) {
                    "小朋友：${message.content}"
                } else {
                    "乐乐：${message.content}"
                }
            }
    }
    
    /**
     * 添加消息到对话
     */
    private fun addMessage(message: ConversationMessage) {
        _conversation.value = _conversation.value + message
    }
    
    override fun onCleared() {
        super.onCleared()
        audioService.release()
    }
}

/**
 * 语音状态
 */
sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    object Speaking : VoiceState()
    data class Error(val message: String) : VoiceState()
}

/**
 * 对话消息
 */
data class ConversationMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)