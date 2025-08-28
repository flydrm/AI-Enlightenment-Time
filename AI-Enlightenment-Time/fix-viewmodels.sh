#!/bin/bash

echo "修复ViewModels..."

# HomeViewModel
cat > app/src/main/java/com/enlightenment/presentation/ui/screens/home/HomeViewModel.kt << 'EOF'
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
EOF

# StoryViewModel  
cat > app/src/main/java/com/enlightenment/presentation/ui/screens/story/StoryViewModel.kt << 'EOF'
package com.enlightenment.presentation.ui.screens.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Story
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoryViewModel : ViewModel() {
    
    private val storyRepository = DIContainer.storyRepository
    
    val stories: StateFlow<List<Story>> = storyRepository.getAllStories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun selectCategory(category: String) {
        // 实现分类过滤逻辑
    }
}
EOF

# CameraViewModel
cat > app/src/main/java/com/enlightenment/presentation/camera/CameraViewModel.kt << 'EOF'
package com.enlightenment.presentation.camera

import android.app.Application
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted
    
    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath
    
    fun onPermissionResult(granted: Boolean) {
        _permissionGranted.value = granted
    }
    
    suspend fun takePicture() {
        // 实现拍照逻辑
    }
    
    fun startCamera() {
        // 实现启动相机逻辑
    }
}
EOF

echo "ViewModels修复完成！"