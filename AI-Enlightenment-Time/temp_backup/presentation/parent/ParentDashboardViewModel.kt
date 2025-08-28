package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.domain.repository.AchievementRepository
import com.enlightenment.domain.repository.UserProgressRepository
import com.enlightenment.domain.usecase.GetDailyProgressUseCase
import com.enlightenment.domain.usecase.GetWeeklyProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val userProgressRepository: UserProgressRepository,
    private val achievementRepository: AchievementRepository,
    private val getDailyProgressUseCase: GetDailyProgressUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            // 加载总学习时长
            userProgressRepository.getTotalLearningTime()
                .onSuccess { time ->
                    _uiState.update { it.copy(totalLearningTime = time) }
                }
            
            // 加载本周进度
            getWeeklyProgressUseCase()
                .onSuccess { progress ->
                    _uiState.update { it.copy(weeklyProgress = progress) }
                }
            
            // 加载连续学习天数
            userProgressRepository.getDailyStreak()
                .onSuccess { streak ->
                    _uiState.update { it.copy(dailyStreak = streak) }
                }
            
            // 加载今日活动
            loadTodayActivities()
            
            // 加载成就进度
            loadAchievementProgress()
        }
    }
    
    private suspend fun loadTodayActivities() {
        getDailyProgressUseCase.getTodayActivities()
            .onSuccess { activities ->
                val formattedActivities = activities.map { activity ->
                    Activity(
                        title = activity.title,
                        time = activity.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                        duration = activity.duration,
                        type = when (activity.type) {
                            "story" -> ActivityType.STORY
                            "exploration" -> ActivityType.EXPLORATION
                            "achievement" -> ActivityType.ACHIEVEMENT
                            "voice" -> ActivityType.VOICE
                            else -> ActivityType.STORY
                        }
                    )
                }
                _uiState.update { it.copy(todayActivities = formattedActivities) }
            }
    }
    
    private suspend fun loadAchievementProgress() {
        achievementRepository.getAllAchievements()
            .onSuccess { achievements ->
                val unlocked = achievements.count { it.isUnlocked }
                val total = achievements.size
                val recent = achievements
                    .filter { it.isUnlocked }
                    .sortedByDescending { it.unlockedAt }
                    .take(3)
                    .map { it.name }
                
                _uiState.update { 
                    it.copy(
                        unlockedAchievements = unlocked,
                        totalAchievements = total,
                        recentAchievements = recent
                    )
                }
            }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
}

data class ParentDashboardUiState(
    val isLoading: Boolean = false,
    val totalLearningTime: Int = 0, // 分钟
    val weeklyProgress: Float = 0f, // 0-1
    val dailyStreak: Int = 0,
    val todayActivities: List<Activity> = emptyList(),
    val unlockedAchievements: Int = 0,
    val totalAchievements: Int = 14,
    val recentAchievements: List<String> = emptyList(),
    val error: String? = null
)