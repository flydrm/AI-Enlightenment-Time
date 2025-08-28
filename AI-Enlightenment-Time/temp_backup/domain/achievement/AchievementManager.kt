package com.enlightenment.domain.achievement

import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.preference.UserPreferences
import com.enlightenment.domain.model.Achievement
import com.enlightenment.security.AuditLogger
import com.enlightenment.security.UserAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 成就系统管理器
 * 负责成就的解锁、进度追踪和奖励发放
 */
@Singleton
class AchievementManager @Inject constructor(
    private val database: AppDatabase,
    private val userPreferences: UserPreferences,
    private val auditLogger: AuditLogger
) {
    
    /**
     * 所有可用的成就定义
     */
    val allAchievements = listOf(
        // 学习时长成就
        Achievement(
            id = "first_story",
            name = "故事初探",
            description = "完成第一个故事",
            icon = "🌟",
            requiredCount = 1,
            category = AchievementCategory.LEARNING,
            points = 10
        ),
        Achievement(
            id = "story_lover",
            name = "故事爱好者",
            description = "完成10个故事",
            icon = "📚",
            requiredCount = 10,
            category = AchievementCategory.LEARNING,
            points = 50
        ),
        Achievement(
            id = "story_master",
            name = "故事大师",
            description = "完成50个故事",
            icon = "👑",
            requiredCount = 50,
            category = AchievementCategory.LEARNING,
            points = 200
        ),
        
        // 连续学习成就
        Achievement(
            id = "three_day_streak",
            name = "三天坚持",
            description = "连续学习3天",
            icon = "🔥",
            requiredCount = 3,
            category = AchievementCategory.CONSISTENCY,
            points = 30
        ),
        Achievement(
            id = "week_warrior",
            name = "一周勇士",
            description = "连续学习7天",
            icon = "⚔️",
            requiredCount = 7,
            category = AchievementCategory.CONSISTENCY,
            points = 100
        ),
        Achievement(
            id = "month_champion",
            name = "月度冠军",
            description = "连续学习30天",
            icon = "🏆",
            requiredCount = 30,
            category = AchievementCategory.CONSISTENCY,
            points = 500
        ),
        
        // 探索成就
        Achievement(
            id = "camera_explorer",
            name = "小小摄影师",
            description = "使用相机拍照10次",
            icon = "📷",
            requiredCount = 10,
            category = AchievementCategory.EXPLORATION,
            points = 40
        ),
        Achievement(
            id = "voice_hero",
            name = "语音小达人",
            description = "使用语音功能20次",
            icon = "🎤",
            requiredCount = 20,
            category = AchievementCategory.EXPLORATION,
            points = 60
        ),
        
        // 学习效果成就
        Achievement(
            id = "quick_learner",
            name = "快速学习者",
            description = "一天内完成3个故事",
            icon = "⚡",
            requiredCount = 3,
            category = AchievementCategory.CREATIVITY,
            points = 80
        ),
        Achievement(
            id = "perfect_week",
            name = "完美一周",
            description = "一周内每天都完成学习",
            icon = "✨",
            requiredCount = 7,
            category = AchievementCategory.CREATIVITY,
            points = 150
        ),
        
        // 特殊成就
        Achievement(
            id = "early_bird",
            name = "早起的小鸟",
            description = "在早上9点前开始学习",
            icon = "🐦",
            requiredCount = 1,
            category = AchievementCategory.CREATIVITY,
            points = 20
        ),
        Achievement(
            id = "night_owl",
            name = "夜猫子",
            description = "在晚上8点后学习",
            icon = "🦉",
            requiredCount = 1,
            category = AchievementCategory.CREATIVITY,
            points = 20
        ),
        Achievement(
            id = "all_categories",
            name = "全能学习者",
            description = "体验所有故事类别",
            icon = "🌈",
            requiredCount = 5,
            category = AchievementCategory.CREATIVITY,
            points = 100
        )
    )
    
    /**
     * 检查并解锁成就
     */
    suspend fun checkAndUnlockAchievements(trigger: AchievementTrigger): List<Achievement> {
        val userId = userPreferences.childName.first()
        val userProgress = database.userProgressDao().getUserProgress(userId)
            ?: return emptyList()
        
        val unlockedAchievements = mutableListOf<Achievement>()
        
        // 获取已解锁的成就ID
        val alreadyUnlocked = userProgress.unlockedAchievements
        
        // 检查每个成就
        for (achievement in allAchievements) {
            if (achievement.id in alreadyUnlocked) continue
            
            val isUnlocked = checkAchievementRequirement(
                achievement.requirement,
                trigger,
                userProgress,
                userId
            )
            
            if (isUnlocked) {
                // 解锁成就
                unlockAchievement(achievement, userId)
                unlockedAchievements.add(achievement)
            }
        }
        
        return unlockedAchievements
    }
    
    /**
     * 检查成就要求是否满足
     */
    private suspend fun checkAchievementRequirement(
        requirement: AchievementRequirement,
        trigger: AchievementTrigger,
        userProgress: com.enlightenment.data.local.entity.UserProgressEntity,
        userId: String
    ): Boolean {
        return when (requirement) {
            is AchievementRequirement.StoriesCompleted -> {
                userProgress.totalStoriesCompleted >= requirement.count
            }
            is AchievementRequirement.DayStreak -> {
                userProgress.currentStreak >= requirement.days
            }
            is AchievementRequirement.PhotosTaken -> {
                trigger == AchievementTrigger.PHOTO_TAKEN &&
                    getPhotoCount(userId) >= requirement.count
            }
            is AchievementRequirement.VoiceInteractions -> {
                trigger == AchievementTrigger.VOICE_USED &&
                    getVoiceInteractionCount(userId) >= requirement.count
            }
            is AchievementRequirement.StoriesInDay -> {
                trigger == AchievementTrigger.STORY_COMPLETED &&
                    getTodayStoryCount(userId) >= requirement.count
            }
            is AchievementRequirement.PerfectWeek -> {
                checkPerfectWeek(userId)
            }
            is AchievementRequirement.EarlyBird -> {
                trigger == AchievementTrigger.SESSION_STARTED &&
                    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) < 9
            }
            is AchievementRequirement.NightOwl -> {
                trigger == AchievementTrigger.SESSION_STARTED &&
                    java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) >= 20
            }
            is AchievementRequirement.AllCategories -> {
                checkAllCategoriesExplored(userId)
            }
        }
    }
    
    /**
     * 解锁成就
     */
    private suspend fun unlockAchievement(achievement: Achievement, userId: String) {
        // 更新数据库
        database.userProgressDao().unlockAchievement(userId, achievement.id)
        
        // 增加积分
        database.userProgressDao().addPoints(userId, achievement.points)
        
        // 记录成就解锁
        auditLogger.logUserAction(
            UserAction.ACHIEVEMENT_UNLOCKED,
            "解锁成就：${achievement.name}",
            mapOf(
                "achievement_id" to achievement.id,
                "points" to achievement.points.toString(),
                "category" to achievement.category.name
            )
        )
    }
    
    /**
     * 获取用户的成就进度
     */
    fun getAchievementProgress(userId: String): Flow<AchievementProgress> = flow {
        val userProgress = database.userProgressDao().getUserProgress(userId)
        
        if (userProgress != null) {
            val unlockedCount = userProgress.unlockedAchievements.size
            val totalCount = allAchievements.size
            val totalPoints = userProgress.totalPoints
            val maxPoints = allAchievements.sumOf { it.points }
            
            emit(AchievementProgress(
                unlockedCount = unlockedCount,
                totalCount = totalCount,
                totalPoints = totalPoints,
                maxPoints = maxPoints,
                unlockedAchievements = userProgress.unlockedAchievements
                    .mapNotNull { id -> allAchievements.find { it.id == id } },
                lockedAchievements = allAchievements
                    .filter { it.id !in userProgress.unlockedAchievements }
            ))
        }
    }
    
    // 辅助方法
    private suspend fun getPhotoCount(userId: String): Int {
        // 从审计日志统计拍照次数
        return 0 // 简化实现
    }
    
    private suspend fun getVoiceInteractionCount(userId: String): Int {
        // 从审计日志统计语音使用次数
        return 0 // 简化实现
    }
    
    private suspend fun getTodayStoryCount(userId: String): Int {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val progress = database.dailyProgressDao().getDailyProgress(userId, today)
        return progress?.storiesCompleted ?: 0
    }
    
    private suspend fun checkPerfectWeek(userId: String): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(java.util.Calendar.DAY_OF_WEEK, -7)
        val startDate = calendar.timeInMillis
        
        val weekProgress = database.dailyProgressDao()
            .getProgressBetweenDates(userId, startDate, endDate)
        
        return weekProgress.size == 7 && weekProgress.all { it.storiesCompleted > 0 }
    }
    
    private suspend fun checkAllCategoriesExplored(userId: String): Boolean {
        val completedStories = database.storyDao().getCompletedStories(userId)
        val categories = completedStories.map { it.category }.distinct()
        return categories.size == com.enlightenment.domain.model.StoryCategory.values().size
    }
}

/**
 * 成就类别
 */
// 使用 com.enlightenment.domain.model.AchievementCategory

/**
 * 成就要求
 */
sealed class AchievementRequirement {
    data class StoriesCompleted(val count: Int) : AchievementRequirement()
    data class DayStreak(val days: Int) : AchievementRequirement()
    data class PhotosTaken(val count: Int) : AchievementRequirement()
    data class VoiceInteractions(val count: Int) : AchievementRequirement()
    data class StoriesInDay(val count: Int) : AchievementRequirement()
    object PerfectWeek : AchievementRequirement()
    object EarlyBird : AchievementRequirement()
    object NightOwl : AchievementRequirement()
    object AllCategories : AchievementRequirement()
}

/**
 * 成就触发器
 */
enum class AchievementTrigger {
    STORY_COMPLETED,
    PHOTO_TAKEN,
    VOICE_USED,
    SESSION_STARTED,
    DAILY_CHECK
}

/**
 * 成就进度
 */
data class AchievementProgress(
    val unlockedCount: Int,
    val totalCount: Int,
    val totalPoints: Int,
    val maxPoints: Int,
    val unlockedAchievements: List<Achievement>,
    val lockedAchievements: List<Achievement>
)