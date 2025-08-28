package com.enlightenment.data.local.dao

import androidx.room.*
import com.enlightenment.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun getUserProgress(userId: String): Flow<UserProgressEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)
    
    @Update
    suspend fun updateProgress(progress: UserProgressEntity)
    
    @Query("UPDATE user_progress SET totalStoriesCompleted = totalStoriesCompleted + 1 WHERE userId = :userId")
    suspend fun incrementStoriesCompleted(userId: String)
    
    @Query("UPDATE user_progress SET totalLearningTime = totalLearningTime + :minutes WHERE userId = :userId")
    suspend fun addLearningTime(userId: String, minutes: Int)
    
    @Query("UPDATE user_progress SET currentStreak = :streak WHERE userId = :userId")
    suspend fun updateCurrentStreak(userId: String, streak: Int)
    
    @Query("UPDATE user_progress SET unlockedAchievements = unlockedAchievements || ',' || :achievementId WHERE userId = :userId")
    suspend fun unlockAchievement(userId: String, achievementId: String)
    
    // TODO: Implement points system if needed
    // @Query("UPDATE user_progress SET totalPoints = totalPoints + :points WHERE userId = :userId")
    // suspend fun addPoints(userId: String, points: Int)
    
    @Query("UPDATE user_progress SET longestStreak = :streak WHERE userId = :userId")
    suspend fun updateLongestStreak(userId: String, streak: Int)
    
    @Query("UPDATE user_progress SET lastActiveDate = :date WHERE userId = :userId")
    suspend fun updateLastActiveDate(userId: String, date: Long)
    
    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteProgress(userId: String)
}