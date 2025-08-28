package com.enlightenment.ai.model.impl

import com.enlightenment.ai.model.TextGenerationModel
import com.enlightenment.data.network.api.*
import com.enlightenment.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



/**
 * GPT-5-PRO文本生成模型实现
 */
class GPT5TextGenerationModel constructor(
    private val openAIApi: OpenAIApi,
    private val secureStorage: SecureStorage
) : TextGenerationModel {
    
    override val name = "GPT-5-PRO"
    override val version = "5.0"
    
    private var initialized = false
    
    override suspend fun isReady(): Boolean = initialized
    
    override suspend fun initialize() {
        val apiKey = secureStorage.getOpenAIApiKey()
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("OpenAI API key not configured")
        }
        initialized = true
    }
    
    override suspend fun release() {
        initialized = false
    }
    
    override suspend fun generateText(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getOpenAIApiKey()
            ?: throw IllegalStateException("OpenAI API key not available")
        
        val request = ChatCompletionRequest(
            model = OpenAIApi.MODEL_NAME,
            messages = listOf(
                ChatMessage.system(getChildFriendlySystemPrompt()),
                ChatMessage.user(prompt)
            ),
            temperature = temperature,
            maxTokens = maxTokens,
            presencePenalty = 0.1f,
            frequencyPenalty = 0.1f
        )
        
        try {
            val response = openAIApi.createChatCompletion("Bearer $apiKey", request)
            
            val choice = response.choices.firstOrNull()
                ?: throw IllegalStateException("No response generated")
            
            return@withContext choice.message.content
            
        } catch (e: Exception) {
            when (e) {
                is retrofit2.HttpException -> {
                    when (e.code()) {
                        429 -> throw RateLimitException("API rate limit exceeded", e)
                        401 -> throw AuthenticationException("Invalid API key", e)
                        else -> throw GenerationException("Failed to generate text: ${e.message()}", e)
                    }
                }
                else -> throw GenerationException("Failed to generate text: ${e.message}", e)
            }
        }
    }
    
    /**
     * 生成高质量儿童内容
     */
    suspend fun generatePremiumChildContent(
        topic: String,
        contentType: ContentType,
        age: Int,
        additionalRequirements: String? = null
    ): String {
        val prompt = buildString {
            appendLine("Create high-quality ${contentType.description} content for a $age-year-old child.")
            appendLine("Topic: $topic")
            appendLine("\nRequirements:")
            appendLine("1. Age-appropriate language and concepts")
            appendLine("2. Educational value without being preachy")
            appendLine("3. Engaging and interactive elements")
            appendLine("4. Safe, positive, and encouraging tone")
            appendLine("5. Cultural sensitivity and inclusiveness")
            
            additionalRequirements?.let {
                appendLine("\nAdditional requirements: $it")
            }
            
            when (contentType) {
                ContentType.STORY -> {
                    appendLine("\nStory elements to include:")
                    appendLine("- Memorable characters with distinct personalities")
                    appendLine("- Clear beginning, middle, and end")
                    appendLine("- A gentle lesson or moral")
                    appendLine("- Vivid descriptions to spark imagination")
                }
                ContentType.EDUCATIONAL -> {
                    appendLine("\nEducational elements to include:")
                    appendLine("- Fun facts presented in an engaging way")
                    appendLine("- Questions to encourage thinking")
                    appendLine("- Real-world connections")
                    appendLine("- Simple explanations of complex concepts")
                }
                ContentType.INTERACTIVE -> {
                    appendLine("\nInteractive elements to include:")
                    appendLine("- Questions for the child to answer")
                    appendLine("- Choices that affect the narrative")
                    appendLine("- Activities or mini-games descriptions")
                    appendLine("- Encouragement for physical movement or creativity")
                }
            }
            
            appendLine("\nPlease create the content now:")
        }
        
        return generateText(prompt, maxTokens = 1000, temperature = 0.8f)
    }
    
    /**
     * 生成对话响应
     */
    suspend fun generateDialogue(
        conversationHistory: List<ChatMessage>,
        userInput: String,
        childAge: Int,
        maxTokens: Int = 150
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getOpenAIApiKey()
            ?: throw IllegalStateException("OpenAI API key not available")
        
        val messages = mutableListOf<ChatMessage>()
        messages.add(ChatMessage.system(getDialogueSystemPrompt(childAge)))
        messages.addAll(conversationHistory.takeLast(10)) // 保留最近10轮对话
        messages.add(ChatMessage.user(userInput))
        
        val request = ChatCompletionRequest(
            model = OpenAIApi.MODEL_NAME,
            messages = messages,
            temperature = 0.7f,
            maxTokens = maxTokens
        )
        
        val response = openAIApi.createChatCompletion("Bearer $apiKey", request)
        return@withContext response.choices.first().message.content
    }
    
    /**
     * 获取儿童友好的系统提示
     */
    private fun getChildFriendlySystemPrompt(): String {
        return """
            You are a friendly, patient, and encouraging AI assistant designed specifically for young children.
            Your responses should be:
            - Simple and easy to understand
            - Warm, positive, and encouraging
            - Safe and appropriate for children
            - Educational but fun
            - Free from any scary, violent, or inappropriate content
            - Culturally sensitive and inclusive
            
            Always maintain a cheerful and supportive tone, like a caring teacher or friendly storyteller.
        """.trimIndent()
    }
    
    /**
     * 获取对话系统提示
     */
    private fun getDialogueSystemPrompt(childAge: Int): String {
        return """
            You are Panda, a friendly AI companion for a $childAge-year-old child.
            
            Personality traits:
            - Cheerful, curious, and playful
            - Patient and understanding
            - Encouraging and supportive
            - Knowledgeable but humble
            
            Communication style:
            - Use simple words appropriate for a $childAge-year-old
            - Ask engaging questions to encourage thinking
            - Respond with enthusiasm to the child's interests
            - Offer gentle corrections when needed
            - Celebrate the child's achievements, no matter how small
            
            Safety guidelines:
            - Never share personal information
            - Redirect inappropriate topics gently
            - Encourage parental involvement when appropriate
            - Promote healthy habits and positive values
        """.trimIndent()
    }
}
/**
 * 内容类型
 */
enum class ContentType(val description: String) {
    STORY("story"),
    EDUCATIONAL("educational content"),
    INTERACTIVE("interactive experience")
}
