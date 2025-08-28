package com.enlightenment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enlightenment.domain.model.DailyProgress

/**
 * 每日进度数据实体
 */
@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey
    val date: Long, // 日期时间戳（当天0点）
    val userId: String,
    val storiesCompleted: Int = 0,
    val learningTimeMinutes: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 转换为领域模型
     */
    fun toDomainModel(): DailyProgress {
        return DailyProgress(
            date = date,
            storiesCompleted = storiesCompleted,
            learningTimeMinutes = learningTimeMinutes,
            questionsAnswered = questionsAnswered,
            correctAnswers = correctAnswers
        )
    }
    
    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromDomainModel(userId: String, progress: DailyProgress): DailyProgressEntity {
            return DailyProgressEntity(
                date = progress.date,
                userId = userId,
                storiesCompleted = progress.storiesCompleted,
                learningTimeMinutes = progress.learningTimeMinutes,
                questionsAnswered = progress.questionsAnswered,
                correctAnswers = progress.correctAnswers
            )
        }
    }
}