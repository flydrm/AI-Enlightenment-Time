package com.enlightenment.presentation.story.player

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
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
