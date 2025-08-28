package com.enlightenment.ai.service.impl

import com.enlightenment.ai.config.AIConfigManager
import com.enlightenment.ai.config.AIModelType
import com.enlightenment.ai.service.ImageGenerationService
import com.enlightenment.ai.service.ImageStyle
import com.enlightenment.data.network.api.GrokImageApi
import com.enlightenment.data.network.api.ImageGenerationRequest
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图像生成服务实现
 * 使用 grok-4-imageGen 模型生成图像
 */
@Singleton
class ImageGenerationServiceImpl @Inject constructor(
    private val configManager: AIConfigManager,
    private val grokImageApi: GrokImageApi,
    private val secureStorage: SecureStorage
) : ImageGenerationService {
    
    override suspend fun generateStoryImage(
        storyTitle: String,
        storyContent: String,
        style: ImageStyle
    ): String? = withContext(Dispatchers.IO) {
        try {
            // 检查配置
            val config = configManager.getConfig(AIModelType.GROK_4_IMAGEGEN)
                ?: return@withContext null
            
            // 检查模型健康状态
            val healthStatus = configManager.getHealthStatus(AIModelType.GROK_4_IMAGEGEN)
            if (!healthStatus.isHealthy || healthStatus.inCircuitBreaker) {
                return@withContext fallbackToPlaceholder(storyTitle, style)
            }
            
            // 获取API密钥
            val apiKey = secureStorage.getGrokApiKey()
                ?: return@withContext fallbackToPlaceholder(storyTitle, style)
            
            // 构建生成提示词
            val prompt = buildStoryImagePrompt(storyTitle, storyContent, style)
            
            // 调用 grok-4-imageGen API
            val request = ImageGenerationRequest.childFriendly(prompt)
            val response = grokImageApi.generateImage("Bearer $apiKey", request)
            
            // 获取生成的图片URL
            val imageUrl = response.data.firstOrNull()?.url
            
            if (imageUrl != null) {
                // 更新健康状态为成功
                configManager.updateHealthStatus(
                    AIModelType.GROK_4_IMAGEGEN,
                    isSuccess = true
                )
                return@withContext imageUrl
            } else {
                return@withContext fallbackToPlaceholder(storyTitle, style)
            }
            
        } catch (e: Exception) {
            // 记录错误并更新健康状态
            configManager.updateHealthStatus(
                AIModelType.GROK_4_IMAGEGEN,
                isSuccess = false,
                errorMessage = e.message
            )
            // 降级到占位图片
            return@withContext fallbackToPlaceholder(storyTitle, style)
        }
    }
    
    override suspend fun generateAchievementImage(
        achievementName: String,
        achievementDescription: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val config = configManager.getConfig(AIModelType.GROK_4_IMAGEGEN)
                ?: return@withContext null
            
            val apiKey = secureStorage.getGrokApiKey()
                ?: return@withContext generatePlaceholderAchievement(achievementName)
            
            val prompt = buildAchievementImagePrompt(achievementName, achievementDescription)
            
            // 调用 grok-4-imageGen API
            val request = ImageGenerationRequest(
                prompt = prompt,
                size = "512x512", // 成就图标使用较小尺寸
                quality = "hd",
                style = "vivid"
            )
            val response = grokImageApi.generateImage("Bearer $apiKey", request)
            
            return@withContext response.data.firstOrNull()?.url
                ?: generatePlaceholderAchievement(achievementName)
            
        } catch (e: Exception) {
            configManager.updateHealthStatus(
                AIModelType.GROK_4_IMAGEGEN,
                isSuccess = false,
                errorMessage = e.message
            )
            return@withContext generatePlaceholderAchievement(achievementName)
        }
    }
    
    override suspend fun generateCharacterAvatar(
        characterName: String,
        characterDescription: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val config = configManager.getConfig(AIModelType.GROK_4_IMAGEGEN)
                ?: return@withContext null
            
            val apiKey = secureStorage.getGrokApiKey()
                ?: return@withContext generatePlaceholderAvatar(characterName)
            
            val prompt = buildCharacterAvatarPrompt(characterName, characterDescription)
            
            // 调用 grok-4-imageGen API
            val request = ImageGenerationRequest(
                prompt = prompt,
                size = "256x256", // 头像使用小尺寸
                quality = "standard",
                style = "vivid"
            )
            val response = grokImageApi.generateImage("Bearer $apiKey", request)
            
            return@withContext response.data.firstOrNull()?.url
                ?: generatePlaceholderAvatar(characterName)
            
        } catch (e: Exception) {
            configManager.updateHealthStatus(
                AIModelType.GROK_4_IMAGEGEN,
                isSuccess = false,
                errorMessage = e.message
            )
            return@withContext generatePlaceholderAvatar(characterName)
        }
    }
    
    private fun buildStoryImagePrompt(
        title: String,
        content: String,
        style: ImageStyle
    ): String {
        val styleDescription = when (style) {
            ImageStyle.CHILDREN_BOOK -> "温暖的儿童绘本插画风格，色彩明亮，线条柔和"
            ImageStyle.WATERCOLOR -> "柔和的水彩画风格，透明感强，色彩自然"
            ImageStyle.CARTOON -> "可爱的卡通风格，角色夸张有趣，色彩鲜艳"
            ImageStyle.PIXEL_ART -> "复古的像素艺术风格，8位色彩，简洁明快"
            ImageStyle.CUTE_ANIMAL -> "超可爱的动物风格，大眼睛，圆润造型"
        }
        
        return """
            |创作一幅儿童故事插画：
            |标题：$title
            |内容概要：${content.take(200)}
            |风格要求：$styleDescription
            |特别要求：
            |1. 必须包含可爱的红色小熊猫作为主角
            |2. 画面温馨友好，适合3-6岁儿童
            |3. 色彩以暖色调为主，突出红色元素
            |4. 背景简洁但有趣，不要太复杂
            |5. 确保画面安全友好，没有任何可能吓到孩子的元素
        """.trimMargin()
    }
    
    private fun buildAchievementImagePrompt(
        name: String,
        description: String
    ): String {
        return """
            |创作一个成就徽章图标：
            |成就名称：$name
            |成就描述：$description
            |风格要求：
            |1. 卡通风格的徽章或奖章
            |2. 包含红色小熊猫元素
            |3. 金色或彩虹色的装饰
            |4. 充满正能量和鼓励感
            |5. 简洁但有纪念意义
        """.trimMargin()
    }
    
    private fun buildCharacterAvatarPrompt(
        name: String,
        description: String
    ): String {
        return """
            |创作一个角色头像：
            |角色名称：$name
            |角色描述：$description
            |风格要求：
            |1. 可爱的卡通风格
            |2. 圆形头像框架
            |3. 表情友好亲切
            |4. 色彩鲜明但柔和
            |5. 适合作为儿童应用的角色头像
        """.trimMargin()
    }
    
    // 降级方法
    private fun fallbackToPlaceholder(title: String, style: ImageStyle): String {
        return generatePlaceholderImage(title, style)
    }
    
    // 占位图片生成方法（用于降级场景）
    private fun generatePlaceholderImage(title: String, style: ImageStyle): String {
        // 使用本地资源或预设的占位图片
        return when (style) {
            ImageStyle.CHILDREN_BOOK -> "android.resource://com.enlightenment/drawable/placeholder_story_book"
            ImageStyle.WATERCOLOR -> "android.resource://com.enlightenment/drawable/placeholder_watercolor"
            ImageStyle.CARTOON -> "android.resource://com.enlightenment/drawable/placeholder_cartoon"
            ImageStyle.PIXEL_ART -> "android.resource://com.enlightenment/drawable/placeholder_pixel"
            ImageStyle.CUTE_ANIMAL -> "android.resource://com.enlightenment/drawable/placeholder_animal"
        }
    }
    
    private fun generatePlaceholderAchievement(name: String): String {
        return "android.resource://com.enlightenment/drawable/placeholder_achievement"
    }
    
    private fun generatePlaceholderAvatar(name: String): String {
        return "android.resource://com.enlightenment/drawable/placeholder_avatar"
    }
}