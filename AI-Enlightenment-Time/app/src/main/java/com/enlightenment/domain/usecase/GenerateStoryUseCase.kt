package com.enlightenment.domain.usecase

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.repository.StoryRepository



class GenerateStoryUseCase(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(
        ageGroup: AgeGroup,
        category: StoryCategory,
        interests: List<String> = emptyList()
    ): Result<Story> {
        return storyRepository.generateStory(
            ageGroup = ageGroup,
            category = category,
            interests = interests
        )
    }
}
