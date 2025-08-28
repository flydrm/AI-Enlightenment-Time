package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.StoryCategory
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
    private lateinit var mockStoryRepository: StoryRepository
    
    private lateinit var useCase: GenerateStoryUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GenerateStoryUseCase(
            storyRepository = mockStoryRepository
        )
    }
    
    @Test
    fun `test invoke generates story successfully`() = runTest {
        // Given
        val ageGroup = AgeGroup.PRESCHOOL
        val category = StoryCategory.ADVENTURE
        val interests = listOf("动物", "冒险")
        val expectedStory = Story(
            id = 1,
            title = "勇敢的小兔子",
            content = "从前有一只小兔子...",
            imageUrl = "https://example.com/rabbit.jpg",
            category = StoryCategory.ADVENTURE.displayName,
            ageGroup = ageGroup.minAge,
            createdAt = System.currentTimeMillis(),
            isFavorite = false,
            readCount = 0
        )
        
        `when`(mockStoryRepository.generateStory(ageGroup, category, interests))
            .thenReturn(Result.success(expectedStory))
        
        // When
        val result = useCase(ageGroup, category, interests)
        
        // Then
        assertTrue(result.isSuccess)
        val story = result.getOrNull()!!
        assertEquals(expectedStory.title, story.title)
        assertEquals(expectedStory.content, story.content)
        assertEquals(expectedStory.imageUrl, story.imageUrl)
        assertEquals(expectedStory.category, story.category)
        assertEquals(expectedStory.ageGroup, story.ageGroup)
        
        verify(mockStoryRepository).generateStory(ageGroup, category, interests)
    }
    
    @Test
    fun `test invoke handles repository failure`() = runTest {
        // Given
        val ageGroup = AgeGroup.KINDERGARTEN
        val category = StoryCategory.SCIENCE
        val errorMessage = "AI服务暂时不可用"
        
        `when`(mockStoryRepository.generateStory(any(), any(), any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
        
        // When
        val result = useCase(ageGroup, category)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        verify(mockStoryRepository).generateStory(ageGroup, category, emptyList())
    }
    
    @Test
    fun `test invoke with empty interests list`() = runTest {
        // Given
        val ageGroup = AgeGroup.TODDLER
        val category = StoryCategory.ANIMAL
        val expectedStory = Story(
            id = 2,
            title = "小熊找妈妈",
            content = "有一天，小熊在森林里...",
            imageUrl = null,
            category = category.displayName,
            ageGroup = ageGroup.minAge,
            createdAt = System.currentTimeMillis(),
            isFavorite = false,
            readCount = 0
        )
        
        `when`(mockStoryRepository.generateStory(ageGroup, category, emptyList()))
            .thenReturn(Result.success(expectedStory))
        
        // When
        val result = useCase(ageGroup, category)
        
        // Then
        assertTrue(result.isSuccess)
        val story = result.getOrNull()!!
        assertEquals(expectedStory.title, story.title)
        assertEquals(expectedStory.content, story.content)
        assertEquals(expectedStory.imageUrl, story.imageUrl)
        verify(mockStoryRepository).generateStory(ageGroup, category, emptyList())
    }
    
    @Test
    fun `test invoke with different age groups`() = runTest {
        // Given
        val testCases = listOf(
            AgeGroup.TODDLER to StoryCategory.DAILY_LIFE,
            AgeGroup.PRESCHOOL to StoryCategory.FAIRY_TALE,
            AgeGroup.KINDERGARTEN to StoryCategory.MORAL
        )
        
        testCases.forEach { (ageGroup, category) ->
            val expectedStory = Story(
                id = 3,
                title = "测试故事",
                content = "内容",
                imageUrl = null,
                category = category.displayName,
                ageGroup = ageGroup.minAge,
                createdAt = System.currentTimeMillis(),
                isFavorite = false,
                readCount = 0
            )
            
            `when`(mockStoryRepository.generateStory(ageGroup, category, emptyList()))
                .thenReturn(Result.success(expectedStory))
            
            // When
            val result = useCase(ageGroup, category)
            
            // Then
            assertTrue(result.isSuccess)
            assertEquals(ageGroup.minAge, result.getOrNull()?.ageGroup)
        }
    }
}