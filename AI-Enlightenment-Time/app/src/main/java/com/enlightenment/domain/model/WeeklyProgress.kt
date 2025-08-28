package com.enlightenment.domain.model

import java.util.Date



data class WeeklyProgress(
    val weekStart: Date = Date(),
    val weekEnd: Date = Date(),
    val dailyProgress: List<DailyProgress> = emptyList(),
    val totalStoriesCompleted: Int = 0,
    val totalLearningMinutes: Int = 0,
    val averageStoriesPerDay: Float = 0f,
    val streakDays: Int = 0
)
