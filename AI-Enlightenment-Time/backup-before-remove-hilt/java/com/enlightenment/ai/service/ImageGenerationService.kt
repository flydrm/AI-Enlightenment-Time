package com.enlightenment.ai.service

/**
 * 图像生成服务接口
 */
interface ImageGenerationService {
    
    /**
     * 生成故事配图
     * @param storyTitle 故事标题
     * @param storyContent 故事内容摘要
     * @param style 图像风格
     * @return 生成的图像URL
     */
    suspend fun generateStoryImage(
        storyTitle: String,
        storyContent: String,
        style: ImageStyle = ImageStyle.CHILDREN_BOOK
    ): String?
    
    /**
     * 生成成就奖励图片
     * @param achievementName 成就名称
     * @param achievementDescription 成就描述
     * @return 生成的图像URL
     */
    suspend fun generateAchievementImage(
        achievementName: String,
        achievementDescription: String
    ): String?
    
    /**
     * 生成角色头像
     * @param characterName 角色名称
     * @param characterDescription 角色描述
     * @return 生成的图像URL
     */
    suspend fun generateCharacterAvatar(
        characterName: String,
        characterDescription: String
    ): String?
}

/**
 * 图像风格枚举
 */
enum class ImageStyle {
    CHILDREN_BOOK,      // 儿童绘本风格
    WATERCOLOR,         // 水彩风格
    CARTOON,           // 卡通风格
    PIXEL_ART,         // 像素艺术风格
    CUTE_ANIMAL        // 可爱动物风格
}