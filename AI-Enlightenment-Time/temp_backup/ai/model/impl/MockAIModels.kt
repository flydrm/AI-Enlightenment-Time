package com.enlightenment.ai.model.impl

import com.enlightenment.ai.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

/**
 * 模拟的文本生成模型
 * 在实际应用中，这里会接入真实的AI API（如OpenAI、通义千问等）
 */
class MockTextGenerationModel @Inject constructor() : TextGenerationModel {
    override val name: String = "MockTextGenerator"
    override val version: String = "1.0.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        delay(500) // 模拟初始化延迟
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): String {
        delay(1000) // 模拟API调用延迟
        
        // 根据prompt生成模拟内容
        return when {
            prompt.contains("故事") -> generateMockStory(prompt)
            prompt.contains("描述") -> generateMockDescription(prompt)
            else -> "这是一个模拟生成的文本回复。在实际应用中，这里会返回AI生成的内容。"
        }
    }
    
    private fun generateMockStory(prompt: String): String {
        val stories = listOf(
            """
            小熊猫乐乐的冒险
            
            在一个阳光明媚的早晨，小熊猫乐乐醒来了。它伸了个懒腰，决定今天要去森林里探险。
            
            乐乐背上小背包，装上最爱的竹子零食，兴高采烈地出发了。森林里的空气真新鲜啊！小鸟在枝头唱歌，蝴蝶在花丛中飞舞。
            
            走着走着，乐乐遇到了一只迷路的小兔子。小兔子眼里含着泪水："我找不到回家的路了。"
            
            善良的乐乐决定帮助小兔子。它们一起寻找，终于在一棵大橡树下找到了兔子的家。小兔子的妈妈非常感谢乐乐。
            
            这次冒险让乐乐明白了：帮助别人是一件很快乐的事情！
            """.trimIndent(),
            
            """
            彩虹桥的秘密
            
            雨后的森林里出现了一道美丽的彩虹。小熊猫乐乐好奇地走近，发现彩虹的尽头有一座神奇的桥。
            
            桥上站着一位彩虹精灵："欢迎你，善良的小熊猫！因为你经常帮助别人，我要送你一个礼物。"
            
            精灵给了乐乐一颗会发光的种子："把它种在你最喜欢的地方，它会长成一棵神奇的树。"
            
            乐乐把种子种在了森林的中心。不久，那里长出了一棵会唱歌的大树，为所有的小动物带来欢乐。
            
            从此，森林变得更加美好了！
            """.trimIndent()
        )
        
        return stories.random()
    }
    
    private fun generateMockDescription(prompt: String): String {
        return """
        哇！我看到了一个很有趣的东西呢！
        
        这看起来像是一个圆圆的、彩色的球。它有红色、蓝色和黄色的条纹，就像彩虹一样漂亮！
        
        你知道吗？球是人类最早发明的玩具之一哦。古代的小朋友也喜欢玩球呢！
        
        我们可以用球来做很多游戏：踢球、拍球、传球...和朋友一起玩球是不是很开心呀？
        """.trimIndent()
    }
}

/**
 * 模拟的图像识别模型
 */
class MockImageRecognitionModel @Inject constructor() : ImageRecognitionModel {
    override val name: String = "MockImageRecognizer"
    override val version: String = "1.0.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        delay(500)
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun recognizeImage(imageData: ByteArray): List<RecognitionResult> {
        delay(800) // 模拟处理延迟
        
        // 返回模拟的识别结果
        val mockResults = listOf(
            listOf(
                RecognitionResult("球", 0.95f),
                RecognitionResult("玩具", 0.88f),
                RecognitionResult("圆形物体", 0.76f)
            ),
            listOf(
                RecognitionResult("猫", 0.92f),
                RecognitionResult("动物", 0.89f),
                RecognitionResult("宠物", 0.85f)
            ),
            listOf(
                RecognitionResult("花", 0.94f),
                RecognitionResult("植物", 0.90f),
                RecognitionResult("向日葵", 0.87f)
            ),
            listOf(
                RecognitionResult("汽车", 0.91f),
                RecognitionResult("交通工具", 0.88f),
                RecognitionResult("轿车", 0.83f)
            )
        )
        
        return mockResults.random()
    }
}

/**
 * 模拟的语音识别模型
 */
class MockSpeechRecognitionModel @Inject constructor() : SpeechRecognitionModel {
    override val name: String = "MockSpeechRecognizer"
    override val version: String = "1.0.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        delay(300)
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun recognizeSpeech(audioData: ByteArray): String {
        delay(600) // 模拟处理延迟
        
        val mockTexts = listOf(
            "我想听一个关于小动物的故事",
            "这是什么东西呀？",
            "我喜欢小熊猫乐乐",
            "再讲一个故事好吗？",
            "我今天学会了新东西"
        )
        
        return mockTexts.random()
    }
}

/**
 * 模拟的语音合成模型
 */
class MockTextToSpeechModel @Inject constructor() : TextToSpeechModel {
    override val name: String = "MockTTS"
    override val version: String = "1.0.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        delay(300)
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun synthesizeSpeech(text: String, voice: VoiceType): ByteArray {
        delay(500) // 模拟处理延迟
        
        // 返回模拟的音频数据
        // 在实际应用中，这里会返回真实的音频字节数组
        return ByteArray(text.length * 100) { Random.nextBytes(1)[0] }
    }
}