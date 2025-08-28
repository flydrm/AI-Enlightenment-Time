package com.enlightenment.presentation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Story
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class HomeViewModel : ViewModel() {
    
    private val storyRepository = DIContainer.storyRepository
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadStories()
        loadUserProgress()
    }

    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { stories ->
                _uiState.update { it.copy(
                    recommendedStories = stories.take(5),
                    isLoading = false
                ) }
            }
        }
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            userProgressRepository.getUserProgress("default_user").collect { progress ->
                _uiState.update { it.copy(
                    userProgress = progress
                ) }
            }
        }
    }
}

data class HomeUiState(
    val recommendedStories: List<Story> = emptyList(),
    val userProgress: com.enlightenment.domain.model.UserProgress? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
