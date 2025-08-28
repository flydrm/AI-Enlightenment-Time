package com.enlightenment.presentation.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.usecase.GenerateStoryUseCase
import com.enlightenment.domain.repository.StoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class StoryViewModel constructor(
    private val generateStoryUseCase: GenerateStoryUseCase,
    private val storyRepository: StoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    init {
        loadStories()
        loadCategories()
    }
    
    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { stories ->
                _uiState.update { state ->
                    state.copy(
                        stories = stories,
                        filteredStories = filterStories(
                            stories, 
                            state.selectedCategory,
                            state.selectedAgeGroup
                        )
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        _uiState.update { state ->
            state.copy(
                categories = StoryCategory.values().toList(),
                ageGroups = AgeGroup.values().toList()
            )
        }
    }
    
    fun generateNewStory() {
        val state = _uiState.value
        val ageGroup = state.selectedAgeGroup ?: AgeGroup.PRESCHOOL
        val category = state.selectedCategory ?: StoryCategory.ADVENTURE
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            
            generateStoryUseCase(
                ageGroup = ageGroup,
                category = category,
                interests = state.selectedInterests
            ).fold(
                onSuccess = { story ->
                    _uiState.update { 
                        it.copy(
                            isGenerating = false,
                            generatedStory = story
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isGenerating = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun selectCategory(category: StoryCategory?) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredStories = filterStories(
                    state.stories, 
                    category,
                    state.selectedAgeGroup
                )
            )
        }
    }
    
    fun selectAgeGroup(ageGroup: AgeGroup?) {
        _uiState.update { state ->
            state.copy(
                selectedAgeGroup = ageGroup,
                filteredStories = filterStories(
                    state.stories,
                    state.selectedCategory,
                    ageGroup
                )
            )
        }
    }
    
    fun toggleFavorite(storyId: String) {
        viewModelScope.launch {
            storyRepository.toggleFavorite(storyId)
        }
    }
    
    fun dismissGeneratedStory() {
        _uiState.update { it.copy(generatedStory = null) }
    }
    
    private fun filterStories(
        stories: List<Story>,
        category: StoryCategory?,
        ageGroup: AgeGroup?
    ): List<Story> {
        return stories.filter { story ->
            (category == null || story.category == category) &&
            (ageGroup == null || story.ageGroup == ageGroup)
        }
    }
}

data class StoryUiState(
    val stories: List<Story> = emptyList(),
    val filteredStories: List<Story> = emptyList(),
    val categories: List<StoryCategory> = emptyList(),
    val ageGroups: List<AgeGroup> = emptyList(),
    val selectedCategory: StoryCategory? = null,
    val selectedAgeGroup: AgeGroup? = null,
    val selectedInterests: List<String> = emptyList(),
    val isGenerating: Boolean = false,
    val generatedStory: Story? = null,
    val error: String? = null
)