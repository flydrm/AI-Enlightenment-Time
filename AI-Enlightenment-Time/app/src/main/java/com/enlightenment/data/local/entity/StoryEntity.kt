package com.enlightenment.data.local.entity

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.enlightenment.data.local.converter.Converters
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Question
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory



@Entity(tableName = "stories")
@TypeConverters(Converters::class)
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val audioUrl: String?,
    val duration: Int,
    val ageGroup: AgeGroup,
    val category: StoryCategory,
    val questions: List<Question>,
    val createdAt: Long,
    val isCompleted: Boolean,
    val isFavorite: Boolean
) {
    fun toDomainModel(): Story {
        return Story(
            id = id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            duration = duration,
            ageGroup = ageGroup,
            category = category,
            questions = questions,
            createdAt = createdAt,
            isCompleted = isCompleted,
            isFavorite = isFavorite
        )
    }
    
    companion object {
        fun fromDomainModel(story: Story): StoryEntity {
            return StoryEntity(
                id = story.id,
                title = story.title,
                content = story.content,
                imageUrl = story.imageUrl,
                audioUrl = story.audioUrl,
                duration = story.duration,
                ageGroup = story.ageGroup,
                category = story.category,
                questions = story.questions,
                createdAt = story.createdAt,
                isCompleted = story.isCompleted,
                isFavorite = story.isFavorite
            )
        }
    }
}
