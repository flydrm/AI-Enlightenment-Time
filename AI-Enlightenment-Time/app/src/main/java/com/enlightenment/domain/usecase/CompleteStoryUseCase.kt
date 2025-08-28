package com.enlightenment.domain.usecase

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository



class CompleteStoryUseCase(
    private val storyRepository: StoryRepository,
    private val progressRepository: UserProgressRepository
) {
    suspend operator fun invoke(storyId: String, timeSpentMinutes: Int) {
        // Mark story as completed
        storyRepository.markAsCompleted(storyId)
        
        // Update user progress
        progressRepository.recordStoryCompletion(storyId, timeSpentMinutes)
        
        // Update streak
        progressRepository.updateStreak()
    }
}
