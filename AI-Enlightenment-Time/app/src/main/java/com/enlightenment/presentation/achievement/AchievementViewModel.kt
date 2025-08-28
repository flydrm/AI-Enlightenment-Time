package com.enlightenment.presentation.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.domain.achievement.AchievementCategory
import com.enlightenment.domain.achievement.AchievementManager
import com.enlightenment.domain.achievement.AchievementProgress
import com.enlightenment.domain.model.Achievement
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.UserAction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 成就界面视图模型
 */

class AchievementViewModel constructor(
    private val achievementManager: AchievementManager,
    private val userPreferences: UserPreferences,
    private val auditLogger: AuditLogger
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()
    
    init {
        loadAchievements()
        logAchievementView()
    }
    
    /**
     * 加载成就数据
     */
    private fun loadAchievements() {
        viewModelScope.launch {
            // 获取用户ID
            val userId = userPreferences.childName.first()
            
            // 加载所有成就定义
            _uiState.update { it.copy(allAchievements = achievementManager.allAchievements) }
            
            // 监听成就进度
            achievementManager.getAchievementProgress(userId)
                .collect { progress ->
                    _uiState.update { 
                        it.copy(
                            progress = progress,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    /**
     * 选择成就类别
     */
    fun selectCategory(category: AchievementCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    /**
     * 显示成就详情
     */
    fun showAchievementDetail(achievement: Achievement) {
        _uiState.update { it.copy(selectedAchievement = achievement) }
        
        // 记录查看成就详情
        auditLogger.logUserAction(
            UserAction.ACHIEVEMENT_VIEWED,
            "查看成就详情：${achievement.name}",
            mapOf(
                "achievement_id" to achievement.id,
                "is_unlocked" to (_uiState.value.progress?.unlockedAchievements?.any { it.id == achievement.id } ?: false).toString()
            )
        )
    }
    
    /**
     * 隐藏成就详情
     */
    fun hideAchievementDetail() {
        _uiState.update { it.copy(selectedAchievement = null) }
    }
    
    /**
     * 记录查看成就界面
     */
    private fun logAchievementView() {
        auditLogger.logUserAction(
            UserAction.ACHIEVEMENT_VIEWED,
            "进入成就界面"
        )
    }
}

/**
 * 成就界面UI状态
 */
data class AchievementUiState(
    val isLoading: Boolean = true,
    val allAchievements: List<Achievement> = emptyList(),
    val progress: AchievementProgress? = null,
    val selectedCategory: AchievementCategory? = null,
    val selectedAchievement: Achievement? = null
)