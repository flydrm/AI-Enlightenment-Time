package com.enlightenment.data.local.dao

import androidx.room.*
import com.enlightenment.data.local.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow



/**
 * 每日进度数据访问对象
 */
@Dao
interface DailyProgressDao {
    
    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND date = :date")
    suspend fun getDailyProgress(userId: String, date: Long): DailyProgressEntity?
    
    @Query("SELECT * FROM daily_progress WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getProgressBetweenDates(userId: String, startDate: Long, endDate: Long): List<DailyProgressEntity>
    
    @Query("SELECT * FROM daily_progress WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentProgress(userId: String, limit: Int): List<DailyProgressEntity>
    
    @Query("SELECT * FROM daily_progress WHERE userId = :userId")
    fun getAllProgressFlow(userId: String): Flow<List<DailyProgressEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: DailyProgressEntity)
    
    @Update
    suspend fun update(progress: DailyProgressEntity)
    
    @Delete
    suspend fun delete(progress: DailyProgressEntity)
    
    @Query("DELETE FROM daily_progress WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
    
    @Query("DELETE FROM daily_progress WHERE date < :beforeDate")
    suspend fun deleteOldProgress(beforeDate: Long)
    
    @Query("UPDATE daily_progress SET storiesCompleted = storiesCompleted + 1, updatedAt = :updatedAt WHERE userId = :userId AND date = :date")
    suspend fun incrementStoriesCompleted(userId: String, date: Long, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE daily_progress SET learningTimeMinutes = learningTimeMinutes + :minutes, updatedAt = :updatedAt WHERE userId = :userId AND date = :date")
    suspend fun addLearningTime(userId: String, date: Long, minutes: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE daily_progress SET questionsAnswered = questionsAnswered + 1, correctAnswers = correctAnswers + :correctIncrement, updatedAt = :updatedAt WHERE userId = :userId AND date = :date")
    suspend fun recordQuestionAnswer(userId: String, date: Long, correctIncrement: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM daily_progress WHERE date >= :startTime AND date <= :endTime")
    suspend fun getProgressBetween(startTime: Long, endTime: Long): List<DailyProgressEntity>
}
