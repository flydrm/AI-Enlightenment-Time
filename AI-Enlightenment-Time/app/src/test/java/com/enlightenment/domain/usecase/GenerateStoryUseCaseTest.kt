package com.enlightenment.domain.usecase

import com.enlightenment.domain.model.AgeGroup
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.StoryCategory
import com.enlightenment.domain.repository.StoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateStoryUseCaseTest {

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var generateStoryUseCase: GenerateStoryUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        generateStoryUseCase = GenerateStoryUseCase(storyRepository)
    }

    @Test
    fun `invoke should return success when repository returns story`() = runTest {
        // Given
        val ageGroup = AgeGroup.FOUR_TO_SIX
        val category = StoryCategory.ADVENTURE
        val interests = listOf("dinosaurs", "space")
        val expectedStory = Story(
            id = "1",
            title = "Space Dinosaur Adventure",
            content = "Once upon a time...",
            ageGroup = ageGroup,
            category = category,
            imageUrl = "https://example.com/image.jpg",
            readingTime = 5,
            educationalValue = 0.8f,
            tags = interests
        )
        
        `when`(storyRepository.generateStory(ageGroup, category, interests))
            .thenReturn(Result.success(expectedStory))

        // When
        val result = generateStoryUseCase(ageGroup, category, interests)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedStory, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository returns error`() = runTest {
        // Given
        val ageGroup = AgeGroup.SEVEN_TO_NINE
        val category = StoryCategory.EDUCATIONAL
        val error = Exception("Network error")
        
        `when`(storyRepository.generateStory(ageGroup, category, emptyList()))
            .thenReturn(Result.failure(error))

        // When
        val result = generateStoryUseCase(ageGroup, category)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}