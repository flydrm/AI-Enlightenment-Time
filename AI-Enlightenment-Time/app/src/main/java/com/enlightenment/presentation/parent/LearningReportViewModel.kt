package com.enlightenment.presentation.parent

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.di.DIContainer
import com.enlightenment.domain.model.DailyProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class LearningReportViewModel : ViewModel() {
    private val userProgressRepository = DIContainer.userProgressRepository
    
    private val _uiState = MutableStateFlow(LearningReportUiState())
    val uiState: StateFlow<LearningReportUiState> = _uiState
    
    fun loadWeeklyReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 加载周报逻辑
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                weeklyData = mapOf("周一" to 30, "周二" to 45, "周三" to 60),
                totalLearningMinutes = 180,
                averageMinutesPerDay = 25.7f,
                learningDays = 7,
                learningTrend = listOf(20, 30, 45, 50, 60, 55, 70),
                contentDistribution = mapOf("故事" to 40, "游戏" to 30, "视频" to 30),
                skillProgress = mapOf("认知" to 80, "语言" to 75, "创造力" to 90),
                detailedActivities = listOf(
                    DetailedActivity("阅读故事", 30, System.currentTimeMillis()),
                    DetailedActivity("拍照识物", 20, System.currentTimeMillis())
                )
            )
        }
    }
    
    fun loadMonthlyReport() {
        viewModelScope.launch {
            // 加载月报逻辑
        }
    }
    
    fun loadAllTimeReport() {
        viewModelScope.launch {
            // 加载总报告逻辑
        }
    }
    
    fun exportReport() {
        // 导出报告逻辑
    }
}

data class LearningReportUiState(
    val weeklyData: Map<String, Int> = emptyMap(),
    val activities: List<DetailedActivity> = emptyList(),
    val isLoading: Boolean = false,
    val totalLearningMinutes: Int = 0,
    val averageMinutesPerDay: Float = 0f,
    val learningDays: Int = 0,
    val learningTrend: List<Int> = emptyList(),
    val contentDistribution: Map<String, Int> = emptyMap(),
    val skillProgress: Map<String, Int> = emptyMap(),
    val detailedActivities: List<DetailedActivity> = emptyList()
)

data class DetailedActivity(
    val name: String,
    val duration: Int,
    val timestamp: Long
)
