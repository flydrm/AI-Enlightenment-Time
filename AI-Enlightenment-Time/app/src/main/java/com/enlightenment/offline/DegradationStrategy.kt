package com.enlightenment.offline

import com.enlightenment.ai.model.RecognitionResult
import com.enlightenment.ai.model.VoiceType
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 降级策略管理器
 * 当AI服务不可用时提供降级方案
 */

class DegradationStrategy constructor(
    private val offlineManager: OfflineManager
) {
    
    /**
     * 生成降级故事
     * 当AI故事生成服务不可用时使用
     */
    suspend fun generateDegradedStory(
        theme: String,
        age: Int,
        category: StoryCategory = StoryCategory.ADVENTURE
    ): Story {
        // 模拟生成延迟
        delay(500)
        
        // 从离线模板中选择合适的故事
        val templates = OfflineStoryTemplates.getTemplates(category)
        val template = templates.randomOrNull() ?: createDefaultStory(theme)
        
        return Story(
            id = "degraded_${System.currentTimeMillis()}",
            title = adaptTitle(template.title, theme),
            content = adaptContent(template.content, theme, age),
            chapters = listOf(),
            imageUrl = "android.resource://com.enlightenment/${template.imageResourceId}",
            audioUrl = null,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 降级图像识别
     * 当图像识别服务不可用时使用
     */
    suspend fun degradedImageRecognition(imageData: ByteArray): List<RecognitionResult> {
        // 模拟处理延迟
        delay(300)
        
        // 返回通用识别结果
        return listOf(
            RecognitionResult(
                label = "有趣的物体",
                confidence = 0.8f,
                boundingBox = null
            ),
            RecognitionResult(
                label = "美丽的场景",
                confidence = 0.7f,
                boundingBox = null
            )
        )
    }
    
    /**
     * 生成降级的儿童友好描述
     */
    suspend fun generateDegradedDescription(
        recognitionResults: List<RecognitionResult>,
        childAge: Int
    ): String {
        delay(200)
        
        val descriptions = listOf(
            "哇，你拍了一张很棒的照片！里面有好多有趣的东西呢。",
            "这张照片真漂亮！我看到了一些很特别的东西。",
            "真是个精彩的发现！让我们一起来探索这张照片里的秘密吧。",
            "你的眼光真好！这里面藏着很多有意思的细节。"
        )
        
        return descriptions.random() + when (childAge) {
            3, 4 -> " 你能告诉我你最喜欢照片里的什么吗？"
            5, 6 -> " 这让我想起了一个有趣的故事，你想听吗？"
            else -> " 继续探索，你会发现更多惊喜的！"
        }
    }
    
    /**
     * 降级语音识别
     * 返回预设的识别结果
     */
    suspend fun degradedSpeechRecognition(audioData: ByteArray): String {
        delay(500)
        
        val commonPhrases = listOf(
            "我想听故事",
            "这是什么",
            "真有趣",
            "我喜欢这个",
            "再来一次"
        )
        
        return commonPhrases.random()
    }
    
    /**
     * 降级语音合成
     * 返回预录制的音频或静音
     */
    suspend fun degradedTextToSpeech(
        text: String,
        voice: VoiceType = VoiceType.CHILD_FRIENDLY
    ): ByteArray {
        delay(300)
        
        // 返回空音频数据（实际应用中应该返回预录制的通用音频）
        return ByteArray(0)
    }
    
    /**
     * 检查是否应该使用降级策略
     */
    fun shouldUseDegradation(
        serviceType: ServiceType,
        networkState: NetworkState,
        errorCount: Int = 0
    ): Boolean {
        return when {
            // 无网络连接时全部降级
            networkState == NetworkState.DISCONNECTED -> true
            
            // 网络受限时，根据服务类型决定
            networkState == NetworkState.LIMITED -> when (serviceType) {
                ServiceType.STORY_GENERATION -> true
                ServiceType.IMAGE_GENERATION -> true
                ServiceType.SPEECH_RECOGNITION -> false // 语音识别优先尝试
                ServiceType.TEXT_TO_SPEECH -> false
                ServiceType.IMAGE_RECOGNITION -> true
            }
            
            // 错误次数过多时降级
            errorCount >= 3 -> true
            
            // 否则不降级
            else -> false
        }
    }
    
    /**
     * 创建默认故事
     */
    private fun createDefaultStory(theme: String): StoryTemplate {
        return StoryTemplate(
            title = "关于${theme}的奇妙故事",
            content = """
                今天，小熊猫乐乐要和你分享一个关于${theme}的精彩故事。
                
                在森林的某个角落，发生了一件有趣的事情...
                
                乐乐发现了与${theme}有关的神奇秘密。这个秘密让所有的小动物都感到惊讶和开心。
                
                通过这次冒险，乐乐学到了很多新知识，也明白了${theme}的重要性。
                
                最后，大家都因为这个美好的发现而变得更加快乐。
                
                这个故事告诉我们，只要保持好奇心，生活中处处都有惊喜等着我们去发现！
            """.trimIndent(),
            imageResourceId = "drawable/story_default"
        )
    }
    
    /**
     * 适配故事标题
     */
    private fun adaptTitle(originalTitle: String, theme: String): String {
        return if (theme.isNotBlank() && Random.nextBoolean()) {
            "$theme：$originalTitle"
        } else {
            originalTitle
        }
    }
    
    /**
     * 适配故事内容
     */
    private fun adaptContent(originalContent: String, theme: String, age: Int): String {
        var content = originalContent
        
        // 根据年龄调整语言复杂度
        if (age <= 4) {
            // 简化语言
            content = content
                .replace("特别", "很")
                .replace("立刻", "马上")
                .replace("忍不住", "")
        }
        
        // 插入主题相关内容
        if (theme.isNotBlank() && !content.contains(theme)) {
            content = content.replaceFirst(
                "在一个",
                "在一个充满${theme}的"
            )
        }
        
        return content
    }
    
    /**
     * 获取降级提示消息
     */
    fun getDegradationMessage(serviceType: ServiceType): String {
        return when (serviceType) {
            ServiceType.STORY_GENERATION -> "正在为你准备精彩的故事..."
            ServiceType.IMAGE_GENERATION -> "正在创作美丽的图片..."
            ServiceType.SPEECH_RECOGNITION -> "让我仔细听听你说什么..."
            ServiceType.TEXT_TO_SPEECH -> "正在准备声音..."
            ServiceType.IMAGE_RECOGNITION -> "让我看看这是什么..."
        }
    }
}

/**
 * 服务类型
 */
enum class ServiceType {
    STORY_GENERATION,    // 故事生成
    IMAGE_GENERATION,    // 图片生成
    SPEECH_RECOGNITION,  // 语音识别
    TEXT_TO_SPEECH,     // 语音合成
    IMAGE_RECOGNITION   // 图像识别
}