package com.enlightenment.presentation.story.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class StoryPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val storyRepository = DIContainer.storyRepository
    
    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory
    
    fun loadStory(storyId: String) {
        // 加载故事逻辑
    }
}
