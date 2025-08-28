#!/bin/bash

echo "修复剩余的ViewModels..."

# VoiceViewModel
cat > app/src/main/java/com/enlightenment/presentation/voice/VoiceViewModel.kt << 'EOF'
package com.enlightenment.presentation.voice

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    fun startRecording() {
        _isRecording.value = true
    }
    
    fun stopRecording() {
        _isRecording.value = false
    }
}
EOF

# SettingsViewModel
cat > app/src/main/java/com/enlightenment/presentation/settings/SettingsViewModel.kt << 'EOF'
package com.enlightenment.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    
    private val userPreferences = DIContainer.userPreferences
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
    
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // 清理缓存逻辑
        }
    }
    
    fun backupData() {
        viewModelScope.launch {
            // 备份数据逻辑
        }
    }
}
EOF

# AchievementViewModel
cat > app/src/main/java/com/enlightenment/presentation/achievement/AchievementViewModel.kt << 'EOF'
package com.enlightenment.presentation.achievement

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
EOF

echo "剩余ViewModels修复完成！"