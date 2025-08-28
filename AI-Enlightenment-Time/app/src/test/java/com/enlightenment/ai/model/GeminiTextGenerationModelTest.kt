package com.enlightenment.ai.model

import com.enlightenment.ai.model.impl.GeminiTextGenerationModel
import com.enlightenment.ai.service.GeminiService
import com.enlightenment.data.network.api.GeminiApi
import com.enlightenment.data.network.api.GeminiRequest
import com.enlightenment.data.network.api.GeminiResponse
import com.enlightenment.data.network.api.Content
import com.enlightenment.data.network.api.Candidate
import com.enlightenment.data.network.api.Part
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeminiTextGenerationModelTest {
    
    @Mock
    private lateinit var mockGeminiService: GeminiService
    
    @Mock
    private lateinit var mockGeminiApi: GeminiApi
    
    private lateinit var model: GeminiTextGenerationModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        model = GeminiTextGenerationModel(mockGeminiService)
    }
    
    @Test
    fun `test generateStory returns success with valid response`() = runTest {
        // Given
        val prompt = "创建一个关于小猫的故事"
        val expectedStory = "从前有一只可爱的小猫..."
        val mockResponse = GeminiResponse(
            candidates = listOf(
                Candidate(
                    content = Content(
                        role = "model",
                        parts = listOf(Part.TextPart(expectedStory))
                    ),
                    finishReason = "STOP",
                    index = 0,
                    safetyRatings = emptyList()
                )
            ),
            promptFeedback = null
        )
        
        `when`(mockGeminiService.generateContent(any())).thenReturn(Result.success(expectedStory))
        
        // When
        val result = model.generateStory(prompt, "冒险", 5)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedStory, result.getOrNull())
        verify(mockGeminiService).generateContent(anyString())
    }
    
    @Test
    fun `test generateStory handles empty response`() = runTest {
        // Given
        val prompt = "创建一个故事"
        `when`(mockGeminiService.generateContent(any())).thenReturn(Result.success(""))
        
        // When
        val result = model.generateStory(prompt, "冒险", 5)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("生成的故事内容为空", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `test generateStory handles service failure`() = runTest {
        // Given
        val prompt = "创建一个故事"
        val errorMessage = "网络连接失败"
        `when`(mockGeminiService.generateContent(any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
        
        // When
        val result = model.generateStory(prompt, "冒险", 5)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `test chat returns valid response`() = runTest {
        // Given
        val message = "你好，我叫小明"
        val context = listOf("之前的对话")
        val expectedResponse = "你好小明！很高兴认识你！"
        
        `when`(mockGeminiService.generateContent(any()))
            .thenReturn(Result.success(expectedResponse))
        
        // When
        val result = model.chat(message, context)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }
    
    @Test
    fun `test generateLearningContent creates age-appropriate content`() = runTest {
        // Given
        val topic = "太阳系"
        val ageGroup = 5
        val expectedContent = "太阳系就像一个大家庭..."
        
        `when`(mockGeminiService.generateContent(contains("5岁")))
            .thenReturn(Result.success(expectedContent))
        
        // When
        val result = model.generateLearningContent(topic, ageGroup)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedContent, result.getOrNull())
        verify(mockGeminiService).generateContent(contains("5岁"))
    }
}