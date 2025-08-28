package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.repository.StoryRepository
import javax.inject.Inject

class GenerateStoryUseCase @Inject constructor(
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