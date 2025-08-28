package com.enlightenment.domain.model

import java.util.Date



data class DailyProgress(
    val id: String = "",
    val userId: String,
    val date: Date,
    val storiesCompleted: Int = 0,
    val totalMinutesSpent: Int = 0,
    val activitiesCompleted: List<String> = emptyList()
)
