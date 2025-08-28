package com.enlightenment.presentation.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class StoryViewModel : ViewModel() {
    
    private val storyRepository = DIContainer.storyRepository
    private val generateStoryUseCase = DIContainer.generateStoryUseCase
    
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    init {
        loadStories()
    }
    
    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { stories ->
                _stories.value = stories
            }
        }
    }
    
    fun selectCategory(category: String) {
        // 实现分类过滤逻辑
    }
    
    fun selectAgeGroup(ageGroup: AgeGroup) {
        // 实现年龄组过滤逻辑
    }
    
    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            storyRepository.toggleFavorite(storyId)
        }
    }
    
    fun generateNewStory() {
        viewModelScope.launch {
            _isLoading.value = true
            // 生成新故事逻辑
            _isLoading.value = false
        }
    }
    
    fun dismissGeneratedStory() {
        // 关闭生成的故事
    }
}

data class StoryUiState(
    val selectedCategory: StoryCategory? = null,
    val selectedAgeGroup: AgeGroup? = null,
    val generatedStory: Story? = null,
    val isGenerating: Boolean = false
)
