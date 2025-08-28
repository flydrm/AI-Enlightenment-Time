package com.enlightenment.data.repository

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import com.enlightenment.ai.service.AIService
import com.enlightenment.data.local.dao.StoryDao
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



class StoryRepositoryImpl(
    private val storyDao: StoryDao,
    private val aiService: AIService
) : StoryRepository {
    
    override suspend fun generateStory(
        ageGroup: AgeGroup,
        category: StoryCategory,
        interests: List<String>
    ): Result<Story> {
        return try {
            // 确保AI服务已初始化
            if (!aiService.isReady.value) {
                aiService.initialize()
            }
            
            // 生成故事
            val generatedStory = aiService.storyGenerator.generateStory(
                ageGroup = ageGroup,
                category = category,
                interests = interests,
                duration = 300 // 5分钟的故事
            )
            
            // 保存生成的故事
            saveStory(generatedStory)
            
            Result.success(generatedStory)
        } catch (e: Exception) {
            // 如果AI生成失败，返回一个预设的故事
            val fallbackStory = createFallbackStory(ageGroup, category, interests)
            saveStory(fallbackStory)
            Result.success(fallbackStory)
        }
    }
    
    private fun createFallbackStory(
        ageGroup: AgeGroup,
        category: StoryCategory,
        interests: List<String>
    ): Story {
        // 根据不同类别创建不同的预设故事
        val (title, content) = when (category) {
            StoryCategory.ADVENTURE -> "小熊猫的森林探险" to 
                """在一个阳光明媚的早晨，小熊猫乐乐决定去森林深处探险。
                |它背上小背包，装满了竹子和水，开始了奇妙的旅程。
                |在路上，乐乐遇到了许多有趣的朋友，学到了很多新知识。
                |最后，乐乐安全回到家，和妈妈分享了今天的冒险故事。""".trimMargin()
            
            StoryCategory.SCIENCE -> "神奇的彩虹" to
                """雨后的天空出现了美丽的彩虹，小熊猫乐乐好奇地问妈妈："为什么会有彩虹呢？"
                |妈妈温柔地解释："当阳光穿过小水滴时，就会变成七种颜色。"
                |乐乐数着彩虹的颜色：红、橙、黄、绿、蓝、靛、紫，真是太神奇了！
                |从此，每次雨后乐乐都会寻找天空中的彩虹。""".trimMargin()
            
            StoryCategory.NATURE -> "春天的花园" to
                """春天来了，小熊猫乐乐和妈妈一起在花园里种花。
                |它们种下了向日葵、玫瑰和郁金香的种子。
                |每天，乐乐都给花儿浇水，看着它们慢慢发芽、长大。
                |几周后，花园里开满了五颜六色的花朵，蝴蝶和蜜蜂都来做客了。""".trimMargin()
            
            StoryCategory.SOCIAL -> "分享的快乐" to
                """小熊猫乐乐有一盒新彩笔，它的好朋友小兔子很羡慕。
                |乐乐想了想，决定和小兔子一起分享彩笔。
                |它们一起画画，画了蓝天、白云和绿草地。
                |乐乐发现，和朋友分享让快乐变成了双倍！""".trimMargin()
            
            StoryCategory.CREATIVITY -> "神奇的画笔" to
                """小熊猫乐乐得到了一支神奇的画笔，画什么就能变成什么。
                |它画了一个大苹果，苹果真的出现了！
                |它又画了一只蝴蝶，蝴蝶飞了起来！
                |最后，乐乐画了一个大大的拥抱，送给了妈妈。""".trimMargin()
        }
        
        return Story(
            title = title,
            content = content,
            duration = 300,
            ageGroup = ageGroup,
            category = category,
            questions = listOf(
                "故事里的小熊猫叫什么名字？",
                "你最喜欢故事的哪个部分？"
            ),
            imageUrl = generateStoryImage(title, content)
        )
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
    
    /**
     * 生成故事配图URL
     * 在实际实现中，这里会调用图像生成AI服务
     * 目前返回占位符URL
     */
    private fun generateStoryImage(title: String, content: String): String {
        // 生成基于标题的唯一图片标识
        val imageId = title.hashCode().toString()
        // 返回占位符URL，实际应用中会调用Grok-4或其他图像生成服务
        return "https://picsum.photos/seed/$imageId/800/600"
    }
}
