package com.enlightenment.domain.usecase

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.repository.StoryRepository
import javax.inject.Inject



class GetStoryByIdUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend fun execute(storyId: String): Story? {
        return storyRepository.getStoryById(storyId)
    }
}
