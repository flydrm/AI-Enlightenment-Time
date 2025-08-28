package com.enlightenment.domain.model

data class WeeklyProgress(
    val weekStart: Long,
    val weekEnd: Long,
    val dailyProgress: List<DailyProgress>,
    val totalStoriesCompleted: Int,
    val totalLearningMinutes: Int,
    val averageStoriesPerDay: Double,
    val streakDays: Int
)