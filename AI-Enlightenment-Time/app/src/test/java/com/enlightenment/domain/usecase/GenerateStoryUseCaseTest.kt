package com.enlightenment.domain.usecase

import com.enlightenment.ai.model.AIModel
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.repository.StoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateStoryUseCaseTest {
    
    @Mock
    private lateinit var mockTextGenerationModel: AIModel.TextGeneration
    
    @Mock
    private lateinit var mockImageGenerationModel: AIModel.ImageGeneration
    
    @Mock
    private lateinit var mockStoryRepository: StoryRepository
    
    private lateinit var useCase: GenerateStoryUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GenerateStoryUseCase(
            textGenerationModel = mockTextGenerationModel,
            imageGenerationModel = mockImageGenerationModel,
            storyRepository = mockStoryRepository
        )
    }
    
    @Test
    fun `test invoke generates story successfully`() = runTest {
        // Given
        val prompt = "一个关于勇敢的小兔子的故事"
        val category = "冒险"
        val ageGroup = 5
        val expectedTitle = "勇敢的小兔子"
        val expectedContent = "从前有一只小兔子..."
        val expectedImageUrl = "https://example.com/rabbit.jpg"
        
        `when`(mockTextGenerationModel.generateStory(prompt, category, ageGroup))
            .thenReturn(Result.success("$expectedTitle\n\n$expectedContent"))
        
        `when`(mockImageGenerationModel.generateImage(any()))
            .thenReturn(Result.success(expectedImageUrl))
        
        `when`(mockStoryRepository.saveStory(any()))
            .thenReturn(Result.success(1L))
        
        // When
        val result = useCase(prompt, category, ageGroup)
        
        // Then
        assertTrue(result.isSuccess)
        val story = result.getOrNull()!!
        assertEquals(expectedTitle, story.title)
        assertEquals(expectedContent, story.content)
        assertEquals(expectedImageUrl, story.imageUrl)
        assertEquals(category, story.category)
        assertEquals(ageGroup, story.ageGroup)
        
        verify(mockStoryRepository).saveStory(any())
    }
    
    @Test
    fun `test invoke handles text generation failure`() = runTest {
        // Given
        val prompt = "故事提示"
        val errorMessage = "AI服务暂时不可用"
        
        `when`(mockTextGenerationModel.generateStory(any(), any(), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
        
        // When
        val result = useCase(prompt, "冒险", 5)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        verify(mockImageGenerationModel, never()).generateImage(any())
        verify(mockStoryRepository, never()).saveStory(any())
    }
    
    @Test
    fun `test invoke continues when image generation fails`() = runTest {
        // Given
        val prompt = "故事提示"
        val storyText = "标题\n\n内容"
        
        `when`(mockTextGenerationModel.generateStory(any(), any(), any()))
            .thenReturn(Result.success(storyText))
        
        `when`(mockImageGenerationModel.generateImage(any()))
            .thenReturn(Result.failure(Exception("图像生成失败")))
        
        `when`(mockStoryRepository.saveStory(any()))
            .thenReturn(Result.success(1L))
        
        // When
        val result = useCase(prompt, "冒险", 5)
        
        // Then
        assertTrue(result.isSuccess)
        val story = result.getOrNull()!!
        assertEquals("", story.imageUrl) // 图像生成失败时使用空URL
        verify(mockStoryRepository).saveStory(any())
    }
    
    @Test
    fun `test invoke extracts title from story content`() = runTest {
        // Given
        val storyWithTitle = "神奇的森林冒险\n\n在一片神奇的森林里..."
        val storyWithoutTitle = "从前有一个小朋友..."
        
        `when`(mockTextGenerationModel.generateStory(any(), any(), any()))
            .thenReturn(Result.success(storyWithTitle))
            .thenReturn(Result.success(storyWithoutTitle))
        
        `when`(mockImageGenerationModel.generateImage(any()))
            .thenReturn(Result.success(""))
        
        `when`(mockStoryRepository.saveStory(any()))
            .thenReturn(Result.success(1L))
        
        // When - 有标题的情况
        val result1 = useCase("prompt1", "冒险", 5)
        assertTrue(result1.isSuccess)
        assertEquals("神奇的森林冒险", result1.getOrNull()?.title)
        
        // When - 无标题的情况
        val result2 = useCase("prompt2", "冒险", 5)
        assertTrue(result2.isSuccess)
        assertEquals("AI生成的故事", result2.getOrNull()?.title) // 使用默认标题
    }
}