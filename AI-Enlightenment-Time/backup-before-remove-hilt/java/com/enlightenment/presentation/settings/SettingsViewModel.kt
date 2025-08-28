package com.enlightenment.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.domain.repository.SettingsRepository
import com.enlightenment.domain.usecase.BackupDataUseCase
import com.enlightenment.domain.usecase.ClearCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupDataUseCase: BackupDataUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // 加载所有设置
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        dailyLearningMinutes = settings.dailyLearningMinutes,
                        reminderTime = settings.reminderTime,
                        restDays = settings.restDays,
                        difficultyLevel = settings.difficultyLevel,
                        contentFilterEnabled = settings.contentFilterEnabled,
                        interestTags = settings.interestTags,
                        preferredAIModel = settings.preferredAIModel,
                        offlineModeEnabled = settings.offlineModeEnabled,
                        analyticsEnabled = settings.analyticsEnabled,
                        soundEnabled = settings.soundEnabled,
                        language = settings.language,
                        theme = settings.theme,
                        lastBackupDate = formatDate(settings.lastBackupDate),
                        appVersion = "1.0.0", // 从BuildConfig获取
                        cacheSize = calculateCacheSize()
                    )
                }
            }
        }
    }
    
    fun updateDailyLearningTime(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateDailyLearningTime(minutes)
            dismissDialog()
        }
    }
    
    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            settingsRepository.updateReminderTime(time)
            dismissDialog()
        }
    }
    
    fun toggleContentFilter() {
        viewModelScope.launch {
            val newValue = !_uiState.value.contentFilterEnabled
            settingsRepository.updateContentFilter(newValue)
        }
    }
    
    fun toggleOfflineMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.offlineModeEnabled
            settingsRepository.updateOfflineMode(newValue)
        }
    }
    
    fun toggleAnalytics() {
        viewModelScope.launch {
            val newValue = !_uiState.value.analyticsEnabled
            settingsRepository.updateAnalytics(newValue)
        }
    }
    
    fun toggleSound() {
        viewModelScope.launch {
            val newValue = !_uiState.value.soundEnabled
            settingsRepository.updateSound(newValue)
        }
    }
    
    fun backupData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            backupDataUseCase()
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            lastBackupDate = formatDate(Date())
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "备份失败: ${error.message}"
                        )
                    }
                }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            clearCacheUseCase()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cacheSize = "0 MB"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "清除缓存失败: ${error.message}"
                        )
                    }
                }
        }
    }
    
    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            loadSettings()
        }
    }
    
    // 对话框管理
    fun showDailyTimeDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.DailyTime) }
    }
    
    fun showReminderTimeDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.ReminderTime) }
    }
    
    fun showRestDaysDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.RestDays) }
    }
    
    fun showDifficultyDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.Difficulty) }
    }
    
    fun showAIModelDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.AIModel) }
    }
    
    fun showLanguageDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.Language) }
    }
    
    fun showThemeDialog() {
        _uiState.update { it.copy(currentDialog = SettingsDialog.Theme) }
    }
    
    fun dismissDialog() {
        _uiState.update { it.copy(currentDialog = null) }
    }
    
    private fun formatDate(date: Date?): String {
        return date?.let {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it)
        } ?: "从未备份"
    }
    
    private suspend fun calculateCacheSize(): String {
        // 实际实现应该计算真实的缓存大小
        return "128 MB"
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    // 学习设置
    val dailyLearningMinutes: Int = 15,
    val reminderTime: String = "19:00",
    val restDays: List<String> = emptyList(),
    // 内容设置
    val difficultyLevel: String = "适中",
    val contentFilterEnabled: Boolean = true,
    val interestTags: List<String> = emptyList(),
    // AI设置
    val preferredAIModel: String = "自动选择",
    val offlineModeEnabled: Boolean = true,
    // 隐私与安全
    val analyticsEnabled: Boolean = false,
    val lastBackupDate: String = "从未备份",
    // 通用设置
    val language: String = "简体中文",
    val theme: String = "跟随系统",
    val soundEnabled: Boolean = true,
    // 关于
    val appVersion: String = "1.0.0",
    val cacheSize: String = "0 MB",
    // UI状态
    val currentDialog: SettingsDialog? = null,
    val error: String? = null
)

sealed class SettingsDialog {
    object DailyTime : SettingsDialog()
    object ReminderTime : SettingsDialog()
    object RestDays : SettingsDialog()
    object Difficulty : SettingsDialog()
    object AIModel : SettingsDialog()
    object Language : SettingsDialog()
    object Theme : SettingsDialog()
}