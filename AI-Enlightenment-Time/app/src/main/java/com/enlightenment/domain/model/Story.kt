package com.enlightenment.domain.model

import java.util.UUID



data class Story(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val duration: Int, // in seconds
    val ageGroup: AgeGroup,
    val category: StoryCategory,
    val questions: List<Question> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val coverImage: String? = null,
    val genre: String = "",
    val readTime: Int = 5 // in minutes
)
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)
enum class AgeGroup(val minAge: Int, val maxAge: Int, val displayName: String) {
    TODDLER(3, 4, "3-4岁"),
    PRESCHOOL(4, 5, "4-5岁"),
    KINDERGARTEN(5, 6, "5-6岁")
}
enum class StoryCategory(val displayName: String, val icon: String) {
    ADVENTURE("冒险故事", "🏔️"),
    ANIMAL("动物朋友", "🐾"),
    SCIENCE("科学探索", "🔬"),
    FAIRY_TALE("童话世界", "🏰"),
    DAILY_LIFE("生活故事", "🏠"),
    MORAL("品德教育", "💝")
}
