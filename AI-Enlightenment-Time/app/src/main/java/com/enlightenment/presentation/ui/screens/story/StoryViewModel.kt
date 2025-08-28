package com.enlightenment.presentation.ui.screens.story

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
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
    
    val categories = StoryCategory.values().toList()
    val ageGroups = AgeGroup.values().toList()
    
    private val _filteredStories = MutableStateFlow<List<Story>>(emptyList())
    val filteredStories: StateFlow<List<Story>> = _filteredStories.asStateFlow()
    
    init {
        loadStories()
    }
    
    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { storyList ->
                _stories.value = storyList
                filterStories()
            }
        }
    }
    
    private fun filterStories() {
        val category = _uiState.value.selectedCategory
        val ageGroup = _uiState.value.selectedAgeGroup
        
        _filteredStories.value = _stories.value.filter { story ->
            (category == null || story.category == category) &&
            (ageGroup == null || story.ageGroup == ageGroup)
        }
    }
    
    fun selectCategory(category: String) {
        val storyCategory = StoryCategory.values().find { it.name == category }
        _uiState.update { it.copy(selectedCategory = storyCategory) }
        filterStories()
    }
    
    fun selectAgeGroup(ageGroup: AgeGroup) {
        _uiState.update { it.copy(selectedAgeGroup = ageGroup) }
        filterStories()
    }
    
    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            storyRepository.toggleFavorite(storyId)
        }
    }
    
    fun generateNewStory() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.update { it.copy(isGenerating = true) }
            // 生成新故事逻辑
            _isLoading.value = false
            _uiState.update { it.copy(isGenerating = false) }
        }
    }
    
    fun dismissGeneratedStory() {
        _uiState.update { it.copy(generatedStory = null) }
    }
}

data class StoryUiState(
    val selectedCategory: StoryCategory? = null,
    val selectedAgeGroup: AgeGroup? = null,
    val generatedStory: Story? = null,
    val isGenerating: Boolean = false
)
