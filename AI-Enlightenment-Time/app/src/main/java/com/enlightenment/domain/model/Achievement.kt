package com.enlightenment.domain.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val requiredCount: Int,
    val currentCount: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val category: AchievementCategory,
    val points: Int = 0
)

enum class AchievementCategory(val displayName: String) {
    STORY("故事达人"),
    LEARNING("学习之星"),
    EXPLORATION("探索家"),
    CONSISTENCY("坚持不懈"),
    CREATIVITY("创意无限")
}