package com.enlightenment.domain.model

data class UserProgress(
    val userId: String,
    val totalStoriesCompleted: Int = 0,
    val totalLearningTime: Int = 0, // in minutes
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val favoriteCategories: List<StoryCategory> = emptyList(),
    val completedStoryIds: Set<String> = emptySet(),
    val unlockedAchievements: Set<String> = emptySet(),
    val totalPoints: Int = 0,
    val totalReadingMinutes: Int = 0
)

data class DailyProgress(
    val date: Long,
    val storiesCompleted: Int = 0,
    val learningTimeMinutes: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0
)