package com.enlightenment.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.domain.repository.DailyProgressRepository
import com.enlightenment.domain.repository.UserProgressRepository
import com.enlightenment.domain.usecase.GenerateReportUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate


class LearningReportViewModel constructor(
    private val userProgressRepository: UserProgressRepository,
    private val dailyProgressRepository: DailyProgressRepository,
    private val generateReportUseCase: GenerateReportUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LearningReportUiState())
    val uiState: StateFlow<LearningReportUiState> = _uiState.asStateFlow()
    
    init {
        loadWeeklyReport()
    }
    
    fun loadWeeklyReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(6)
            
            loadReportData(startDate, endDate, "本周")
        }
    }
    
    fun loadMonthlyReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(29)
            
            loadReportData(startDate, endDate, "本月")
        }
    }
    
    fun loadAllTimeReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 获取第一条记录的日期作为开始日期
            dailyProgressRepository.getFirstProgressDate()
                .onSuccess { firstDate ->
                    val endDate = LocalDate.now()
                    loadReportData(firstDate, endDate, "全部")
                }
                .onFailure {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "无法加载历史数据"
                        )
                    }
                }
        }
    }
    
    private suspend fun loadReportData(
        startDate: LocalDate,
        endDate: LocalDate,
        period: String
    ) {
        try {
            // 加载学习时长统计
            val totalMinutes = dailyProgressRepository.getTotalMinutes(startDate, endDate)
            val learningDays = dailyProgressRepository.getLearningDays(startDate, endDate)
            val averageMinutesPerDay = if (learningDays > 0) totalMinutes / learningDays else 0
            
            // 加载学习趋势
            val trendData = dailyProgressRepository.getDailyProgress(startDate, endDate)
                .map { progress ->
                    DailyLearningData(
                        date = progress.date,
                        minutes = progress.totalMinutes
                    )
                }
            
            // 加载内容分布
            val contentDistribution = calculateContentDistribution(startDate, endDate)
            
            // 加载技能进展
            val skillProgress = loadSkillProgress()
            
            // 加载详细活动
            val detailedActivities = dailyProgressRepository.getDetailedActivities(startDate, endDate)
                .map { activity ->
                    DetailedActivity(
                        title = activity.title,
                        type = activity.type,
                        date = activity.date,
                        duration = activity.duration,
                        score = activity.score,
                        description = activity.description
                    )
                }
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalLearningMinutes = totalMinutes,
                    averageMinutesPerDay = averageMinutesPerDay,
                    learningDays = learningDays,
                    learningTrend = trendData,
                    contentDistribution = contentDistribution,
                    skillProgress = skillProgress,
                    detailedActivities = detailedActivities,
                    currentPeriod = period,
                    error = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "加载报告失败: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun calculateContentDistribution(
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<String, Float> {
        val activities = dailyProgressRepository.getActivitiesByType(startDate, endDate)
        val total = activities.values.sum().toFloat()
        
        return if (total > 0) {
            mapOf(
                "故事探索" to (activities["story"] ?: 0) / total,
                "语音互动" to (activities["voice"] ?: 0) / total,
                "图像识别" to (activities["image"] ?: 0) / total,
                "创意表达" to (activities["creative"] ?: 0) / total
            )
        } else {
            mapOf(
                "故事探索" to 0f,
                "语音互动" to 0f,
                "图像识别" to 0f,
                "创意表达" to 0f
            )
        }
    }
    
    private suspend fun loadSkillProgress(): List<SkillProgress> {
        return userProgressRepository.getSkillProgress()
            .map { skill ->
                SkillProgress(
                    name = skill.name,
                    level = skill.level,
                    progress = skill.experienceProgress
                )
            }
    }
    
    fun exportReport() {
        viewModelScope.launch {
            generateReportUseCase.generatePdfReport(_uiState.value)
                .onSuccess { filePath ->
                    // 触发分享
                    _uiState.update {
                        it.copy(exportedFilePath = filePath)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "导出失败: ${error.message}")
                    }
                }
        }
    }
}

data class LearningReportUiState(
    val isLoading: Boolean = false,
    val currentPeriod: String = "本周",
    val totalLearningMinutes: Int = 0,
    val averageMinutesPerDay: Int = 0,
    val learningDays: Int = 0,
    val learningTrend: List<DailyLearningData> = emptyList(),
    val contentDistribution: Map<String, Float> = emptyMap(),
    val skillProgress: List<SkillProgress> = emptyList(),
    val detailedActivities: List<DetailedActivity> = emptyList(),
    val exportedFilePath: String? = null,
    val error: String? = null
)