package com.enlightenment.presentation.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
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
