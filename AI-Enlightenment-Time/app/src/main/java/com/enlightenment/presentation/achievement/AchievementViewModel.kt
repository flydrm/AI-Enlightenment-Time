package com.enlightenment.presentation.achievement

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.ViewModel
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class AchievementViewModel : ViewModel() {
    
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements
    
    init {
        loadAchievements()
    }
    
    private fun loadAchievements() {
        // 加载成就数据
    }
}
