package com.enlightenment.domain.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import kotlinx.coroutines.flow.Flow



interface StoryRepository {
    suspend fun generateStory(
        ageGroup: AgeGroup,
        category: StoryCategory,
        interests: List<String> = emptyList()
    ): Result<Story>
    
    suspend fun getStoryById(id: String): Story?
    
    suspend fun getAllStories(): Flow<List<Story>>
    
    suspend fun getFavoriteStories(): Flow<List<Story>>
    
    suspend fun getStoriesByCategory(category: StoryCategory): Flow<List<Story>>
    
    suspend fun saveStory(story: Story)
    
    suspend fun updateStory(story: Story)
    
    suspend fun deleteStory(storyId: String)
    
    suspend fun markAsCompleted(storyId: String)
    
    suspend fun toggleFavorite(storyId: String)
    
    suspend fun getRecentStories(limit: Int = 10): List<Story>
}
