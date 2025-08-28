package com.enlightenment.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.UserProgress
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val progressRepository: UserProgressRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load recent stories
                val recentStories = storyRepository.getRecentStories(limit = 5)
                
                // Load user progress
                progressRepository.getUserProgress().collect { progress ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            recentStories = recentStories,
                            userProgress = progress,
                            greeting = getTimeBasedGreeting()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun getTimeBasedGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "早上好！准备开始今天的学习吗？"
            in 12..17 -> "下午好！一起来探索新故事吧！"
            in 18..21 -> "晚上好！睡前听个故事怎么样？"
            else -> "欢迎回来！"
        }
    }
    
    fun onFeatureClick(feature: HomeFeature) {
        // Handle feature clicks
        _uiState.update { 
            it.copy(selectedFeature = feature)
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "欢迎回来！",
    val recentStories: List<Story> = emptyList(),
    val userProgress: UserProgress? = null,
    val selectedFeature: HomeFeature? = null,
    val error: String? = null
)

enum class HomeFeature {
    STORY,
    CAMERA,
    VOICE,
    ACHIEVEMENT
}