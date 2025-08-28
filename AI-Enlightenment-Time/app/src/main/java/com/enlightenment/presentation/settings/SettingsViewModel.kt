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
