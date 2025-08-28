package com.enlightenment.data.local.dao

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.room.*
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.domain.model.StoryCategory
import kotlinx.coroutines.flow.Flow



@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteStories(): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE category = :category ORDER BY createdAt DESC")
    fun getStoriesByCategory(category: StoryCategory): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): StoryEntity?
    
    @Query("SELECT * FROM stories ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentStories(limit: Int): List<StoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    @Update
    suspend fun updateStory(story: StoryEntity)
    
    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStory(storyId: String)
    
    @Query("UPDATE stories SET isCompleted = 1 WHERE id = :storyId")
    suspend fun markAsCompleted(storyId: String)
    
    @Query("SELECT * FROM stories WHERE createdAt >= :startTime AND createdAt <= :endTime AND isCompleted = 1")
    suspend fun getCompletedStoriesBetween(startTime: Long, endTime: Long): List<StoryEntity>
    
    @Query("SELECT * FROM stories WHERE createdAt < :cutoffTime")
    suspend fun getStoriesBeforeDate(cutoffTime: Long): List<StoryEntity>
    
    @Query("DELETE FROM stories WHERE createdAt < :cutoffTime")
    suspend fun deleteStoriesBeforeDate(cutoffTime: Long)
    
    @Query("UPDATE stories SET isFavorite = NOT isFavorite WHERE id = :storyId")
    suspend fun toggleFavorite(storyId: String)
    
    @Query("SELECT COUNT(*) FROM stories")
    suspend fun getStoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM stories WHERE isCompleted = 1")
    suspend fun getCompletedStoryCount(): Int
}
