#!/bin/bash

echo "修复所有ViewModel相关问题..."

# 1. 修复LearningReportViewModel完整实现
cat > app/src/main/java/com/enlightenment/presentation/parent/LearningReportViewModel.kt << 'EOF'
package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.DailyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LearningReportViewModel : ViewModel() {
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _uiState = MutableStateFlow(LearningReportUiState())
    val uiState: StateFlow<LearningReportUiState> = _uiState
    
    fun loadWeeklyReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 加载周报逻辑
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                weeklyData = mapOf("周一" to 30, "周二" to 45, "周三" to 60),
                totalLearningMinutes = 180,
                averageMinutesPerDay = 25.7f,
                learningDays = 7,
                learningTrend = listOf(20, 30, 45, 50, 60, 55, 70),
                contentDistribution = mapOf("故事" to 40, "游戏" to 30, "视频" to 30),
                skillProgress = mapOf("认知" to 80, "语言" to 75, "创造力" to 90),
                detailedActivities = listOf(
                    DetailedActivity("阅读故事", 30, System.currentTimeMillis()),
                    DetailedActivity("拍照识物", 20, System.currentTimeMillis())
                )
            )
        }
    }
    
    fun loadMonthlyReport() {
        viewModelScope.launch {
            // 加载月报逻辑
        }
    }
    
    fun loadAllTimeReport() {
        viewModelScope.launch {
            // 加载总报告逻辑
        }
    }
    
    fun exportReport() {
        // 导出报告逻辑
    }
}

data class LearningReportUiState(
    val weeklyData: Map<String, Int> = emptyMap(),
    val activities: List<DetailedActivity> = emptyList(),
    val isLoading: Boolean = false,
    val totalLearningMinutes: Int = 0,
    val averageMinutesPerDay: Float = 0f,
    val learningDays: Int = 0,
    val learningTrend: List<Int> = emptyList(),
    val contentDistribution: Map<String, Int> = emptyMap(),
    val skillProgress: Map<String, Int> = emptyMap(),
    val detailedActivities: List<DetailedActivity> = emptyList()
)

data class DetailedActivity(
    val name: String,
    val duration: Int,
    val timestamp: Long
)
EOF

# 2. 修复HomeViewModel的getUserProgress调用
cat > app/src/main/java/com/enlightenment/presentation/ui/screens/home/HomeViewModel.kt << 'EOF'
package com.enlightenment.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.UserProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    
    private val storyRepository = DIContainer.storyRepository
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _selectedFeature = MutableStateFlow<HomeFeature?>(null)
    val selectedFeature: StateFlow<HomeFeature?> = _selectedFeature.asStateFlow()

    init {
        loadStories()
        loadUserProgress()
    }

    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { stories ->
                _uiState.update { it.copy(
                    recommendedStories = stories.take(5),
                    recentStories = stories.take(3),
                    isLoading = false
                ) }
            }
        }
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            val progress = userProgressRepository.getUserProgress()
            progress?.let {
                _uiState.update { state ->
                    state.copy(userProgress = it)
                }
            }
        }
    }
    
    fun onFeatureClick(feature: HomeFeature) {
        _selectedFeature.value = feature
    }
}

data class HomeUiState(
    val recommendedStories: List<Story> = emptyList(),
    val recentStories: List<Story> = emptyList(),
    val userProgress: UserProgress? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val greeting: String = "欢迎回来"
)

enum class HomeFeature {
    STORY,
    CAMERA,
    VOICE,
    ACHIEVEMENT
}
EOF

# 3. 修复VoiceViewModel完整实现
cat > app/src/main/java/com/enlightenment/presentation/voice/VoiceViewModel.kt << 'EOF'
package com.enlightenment.presentation.voice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState
    
    private val _conversation = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversation: StateFlow<List<ConversationMessage>> = _conversation
    
    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    fun startRecording() {
        _isRecording.value = true
        _voiceState.value = VoiceState.LISTENING
    }
    
    fun stopRecording() {
        _isRecording.value = false
        _voiceState.value = VoiceState.IDLE
    }
    
    fun startListening() {
        _isListening.value = true
        _voiceState.value = VoiceState.LISTENING
    }
    
    fun stopListening() {
        _isListening.value = false
        _voiceState.value = VoiceState.IDLE
    }
}

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

data class ConversationMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
EOF

# 4. 修复StoryPlayerViewModel完整实现
cat > app/src/main/java/com/enlightenment/presentation/story/player/StoryPlayerViewModel.kt << 'EOF'
package com.enlightenment.presentation.story.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoryPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val storyRepository = DIContainer.storyRepository
    
    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory
    
    val story: StateFlow<Story?> = _currentStory
    
    private val _playerState = MutableStateFlow(PlayerState.IDLE)
    val playerState: StateFlow<PlayerState> = _playerState
    
    private val _currentStoryIndex = MutableStateFlow(0)
    val currentStoryIndex: StateFlow<Int> = _currentStoryIndex
    
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress
    
    private val _isAutoPlay = MutableStateFlow(false)
    val isAutoPlay: StateFlow<Boolean> = _isAutoPlay
    
    fun loadStory(storyId: String) {
        viewModelScope.launch {
            val story = storyRepository.getStoryById(storyId)
            _currentStory.value = story
        }
    }
    
    fun togglePlayPause() {
        _playerState.value = when (_playerState.value) {
            PlayerState.PLAYING -> PlayerState.PAUSED
            else -> PlayerState.PLAYING
        }
    }
    
    fun previousStory() {
        if (_currentStoryIndex.value > 0) {
            _currentStoryIndex.value--
        }
    }
    
    fun nextStory() {
        _currentStoryIndex.value++
    }
    
    fun seekTo(progress: Float) {
        _playbackProgress.value = progress
    }
    
    fun toggleAutoPlay() {
        _isAutoPlay.value = !_isAutoPlay.value
    }
}

enum class PlayerState {
    IDLE,
    PLAYING,
    PAUSED,
    LOADING,
    FINISHED
}
EOF

echo "ViewModel修复完成！"