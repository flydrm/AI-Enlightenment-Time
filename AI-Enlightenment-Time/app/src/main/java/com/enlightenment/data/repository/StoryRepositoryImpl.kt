package com.enlightenment.data.repository

import com.enlightenment.data.local.dao.StoryDao
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao,
    // TODO: Add AI service when implemented
) : StoryRepository {
    
    override suspend fun generateStory(
        ageGroup: AgeGroup,
        category: StoryCategory,
        interests: List<String>
    ): Result<Story> {
        // TODO: Implement AI story generation
        // For now, return a mock story
        val mockStory = Story(
            title = "小熊猫的冒险",
            content = "从前，有一只勇敢的小熊猫，它住在一个美丽的森林里...",
            duration = 300,
            ageGroup = ageGroup,
            category = category,
            questions = emptyList()
        )
        
        // Save the generated story
        saveStory(mockStory)
        
        return Result.success(mockStory)
    }
    
    override suspend fun getStoryById(id: String): Story? {
        return storyDao.getStoryById(id)?.toDomainModel()
    }
    
    override suspend fun getAllStories(): Flow<List<Story>> {
        return storyDao.getAllStories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getFavoriteStories(): Flow<List<Story>> {
        return storyDao.getFavoriteStories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getStoriesByCategory(category: StoryCategory): Flow<List<Story>> {
        return storyDao.getStoriesByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun saveStory(story: Story) {
        storyDao.insertStory(StoryEntity.fromDomainModel(story))
    }
    
    override suspend fun updateStory(story: Story) {
        storyDao.updateStory(StoryEntity.fromDomainModel(story))
    }
    
    override suspend fun deleteStory(storyId: String) {
        storyDao.deleteStory(storyId)
    }
    
    override suspend fun markAsCompleted(storyId: String) {
        storyDao.markAsCompleted(storyId)
    }
    
    override suspend fun toggleFavorite(storyId: String) {
        storyDao.toggleFavorite(storyId)
    }
    
    override suspend fun getRecentStories(limit: Int): List<Story> {
        return storyDao.getRecentStories(limit).map { it.toDomainModel() }
    }
}