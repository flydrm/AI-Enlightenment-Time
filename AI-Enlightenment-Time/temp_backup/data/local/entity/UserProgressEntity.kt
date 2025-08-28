package com.enlightenment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.enlightenment.data.local.converter.Converters
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.model.UserProgress

@Entity(tableName = "user_progress")
@TypeConverters(Converters::class)
data class UserProgressEntity(
    @PrimaryKey
    val userId: String,
    val totalStoriesCompleted: Int,
    val totalLearningTime: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val favoriteCategories: List<StoryCategory>,
    val completedStoryIds: Set<String>,
    val unlockedAchievements: Set<String>,
    val totalPoints: Int = 0,
    val totalReadingMinutes: Int = 0
) {
    fun toDomainModel(): UserProgress {
        return UserProgress(
            userId = userId,
            totalStoriesCompleted = totalStoriesCompleted,
            totalLearningTime = totalLearningTime,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastActiveDate = lastActiveDate,
            favoriteCategories = favoriteCategories,
            completedStoryIds = completedStoryIds,
            unlockedAchievements = unlockedAchievements,
            totalPoints = totalPoints,
            totalReadingMinutes = totalReadingMinutes
        )
    }
    
    companion object {
        fun fromDomainModel(progress: UserProgress): UserProgressEntity {
            return UserProgressEntity(
                userId = progress.userId,
                totalStoriesCompleted = progress.totalStoriesCompleted,
                totalLearningTime = progress.totalLearningTime,
                currentStreak = progress.currentStreak,
                longestStreak = progress.longestStreak,
                lastActiveDate = progress.lastActiveDate,
                favoriteCategories = progress.favoriteCategories,
                completedStoryIds = progress.completedStoryIds,
                unlockedAchievements = progress.unlockedAchievements,
                totalPoints = progress.totalPoints,
                totalReadingMinutes = progress.totalReadingMinutes
            )
        }
    }
}