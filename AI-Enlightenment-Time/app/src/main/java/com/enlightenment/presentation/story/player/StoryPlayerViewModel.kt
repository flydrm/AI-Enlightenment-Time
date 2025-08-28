package com.enlightenment.presentation.story.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.service.AIService
import com.enlightenment.domain.model.Story as DomainStory
import com.enlightenment.domain.usecase.story.GetStoryByIdUseCase
import com.enlightenment.multimedia.audio.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 故事播放器ViewModel
 */
@HiltViewModel
class StoryPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getStoryByIdUseCase: GetStoryByIdUseCase,
    private val audioService: AudioService,
    private val aiService: AIService
) : ViewModel() {
    
    companion object {
        private const val TAG = "StoryPlayerViewModel"
    }
    
    // 当前故事
    private val _story = MutableStateFlow<Story?>(null)
    val story: StateFlow<Story?> = _story.asStateFlow()
    
    // 播放器状态
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    // 当前章节索引
    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()
    
    // 播放进度 (0.0 - 1.0)
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()
    
    // 是否自动播放下一章
    private val _isAutoPlay = MutableStateFlow(true)
    val isAutoPlay: StateFlow<Boolean> = _isAutoPlay.asStateFlow()
    
    // 语音合成任务
    private var ttsJob: Job? = null
    
    // 媒体播放器（用于背景音乐）
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * 加载故事
     */
    fun loadStory(storyId: String) {
        viewModelScope.launch {
            try {
                // 从数据库加载故事
                getStoryByIdUseCase(storyId).collect { domainStory ->
                    domainStory?.let {
                        _story.value = mapDomainStoryToPresentation(it)
                        _currentChapterIndex.value = 0
                        _playerState.value = PlayerState.Ready
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load story", e)
                _playerState.value = PlayerState.Error(e.message ?: "加载失败")
            }
        }
    }
    
    /**
     * 播放/暂停
     */
    fun togglePlayPause() {
        when (_playerState.value) {
            is PlayerState.Playing -> pause()
            is PlayerState.Paused, is PlayerState.Ready -> play()
            else -> {}
        }
    }
    
    /**
     * 播放当前章节
     */
    private fun play() {
        val currentStory = _story.value ?: return
        val currentChapter = currentStory.chapters.getOrNull(_currentChapterIndex.value) ?: return
        
        _playerState.value = PlayerState.Playing
        
        // 取消之前的TTS任务
        ttsJob?.cancel()
        
        // 开始新的TTS任务
        ttsJob = viewModelScope.launch {
            try {
                // 使用AI服务进行语音合成
                val audioData = aiService.speechService.textToSpeech(
                    text = currentChapter.content,
                    voice = com.enlightenment.ai.model.VoiceType.CHILD_FRIENDLY
                )
                
                // 播放音频
                audioService.playAudio(audioData)
                
                // 模拟播放进度
                simulatePlaybackProgress(currentChapter.content.length)
                
                // 播放完成后的处理
                onChapterFinished()
                
            } catch (e: CancellationException) {
                // 任务被取消
                Log.d(TAG, "TTS task cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play chapter", e)
                _playerState.value = PlayerState.Error(e.message ?: "播放失败")
            }
        }
    }
    
    /**
     * 暂停播放
     */
    private fun pause() {
        _playerState.value = PlayerState.Paused
        ttsJob?.cancel()
        audioService.stopPlayback()
    }
    
    /**
     * 上一章
     */
    fun previousChapter() {
        if (_currentChapterIndex.value > 0) {
            _currentChapterIndex.value--
            _playbackProgress.value = 0f
            if (_playerState.value is PlayerState.Playing) {
                play()
            }
        }
    }
    
    /**
     * 下一章
     */
    fun nextChapter() {
        val story = _story.value ?: return
        if (_currentChapterIndex.value < story.chapters.size - 1) {
            _currentChapterIndex.value++
            _playbackProgress.value = 0f
            if (_playerState.value is PlayerState.Playing) {
                play()
            }
        }
    }
    
    /**
     * 跳转到指定进度
     */
    fun seekTo(progress: Float) {
        _playbackProgress.value = progress.coerceIn(0f, 1f)
        // TODO: 实现音频跳转
    }
    
    /**
     * 切换自动播放
     */
    fun toggleAutoPlay() {
        _isAutoPlay.value = !_isAutoPlay.value
    }
    
    /**
     * 章节播放完成
     */
    private fun onChapterFinished() {
        val story = _story.value ?: return
        
        if (_isAutoPlay.value && _currentChapterIndex.value < story.chapters.size - 1) {
            // 自动播放下一章
            nextChapter()
        } else if (_currentChapterIndex.value >= story.chapters.size - 1) {
            // 故事结束
            _playerState.value = PlayerState.Finished
            _playbackProgress.value = 1f
        } else {
            // 暂停等待用户操作
            _playerState.value = PlayerState.Paused
        }
    }
    
    /**
     * 模拟播放进度
     */
    private suspend fun simulatePlaybackProgress(textLength: Int) {
        val duration = textLength * 100L // 假设每个字符100ms
        val updateInterval = 100L // 每100ms更新一次进度
        val steps = duration / updateInterval
        
        for (i in 0..steps) {
            if (_playerState.value !is PlayerState.Playing) break
            
            _playbackProgress.value = (i.toFloat() / steps).coerceIn(0f, 1f)
            delay(updateInterval)
        }
    }
    
    /**
     * 将领域层故事映射到表现层
     */
    private fun mapDomainStoryToPresentation(domainStory: DomainStory): Story {
        return Story(
            id = domainStory.id,
            title = domainStory.title,
            chapters = listOf(
                StoryChapter(
                    id = "chapter_1",
                    content = domainStory.content,
                    imageUrl = domainStory.coverImage,
                    audioUrl = null,
                    choices = emptyList()
                )
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsJob?.cancel()
        mediaPlayer?.release()
        audioService.stopPlayback()
    }
}

/**
 * 播放器状态
 */
sealed class PlayerState {
    object Idle : PlayerState()
    object Ready : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Finished : PlayerState()
    data class Error(val message: String) : PlayerState()
}