package com.enlightenment.presentation.ui.screens.home

import app.cash.turbine.test
import com.enlightenment.domain.model.Story
import com.enlightenment.domain.model.UserProgress
import com.enlightenment.domain.repository.StoryRepository
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var progressRepository: UserProgressRepository

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        `when`(storyRepository.getRecentStories(5)).thenReturn(emptyList())
        `when`(progressRepository.getUserProgress()).thenReturn(flowOf(UserProgress()))

        // When
        viewModel = HomeViewModel(storyRepository, progressRepository)

        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)
        }
    }

    @Test
    fun `loadHomeData should update state with stories and progress`() = runTest {
        // Given
        val mockStories = listOf(
            Story(
                id = "1",
                title = "Test Story",
                content = "Content",
                ageGroup = com.enlightenment.domain.model.AgeGroup.FOUR_TO_SIX,
                category = com.enlightenment.domain.model.StoryCategory.ADVENTURE,
                imageUrl = "",
                readingTime = 5,
                educationalValue = 0.8f,
                tags = emptyList()
            )
        )
        val mockProgress = UserProgress(
            totalStoriesCompleted = 10,
            currentStreak = 5,
            totalReadingMinutes = 100
        )

        `when`(storyRepository.getRecentStories(5)).thenReturn(mockStories)
        `when`(progressRepository.getUserProgress()).thenReturn(flowOf(mockProgress))

        // When
        viewModel = HomeViewModel(storyRepository, progressRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(mockStories, state.recentStories)
            assertEquals(mockProgress, state.userProgress)
        }
    }
}