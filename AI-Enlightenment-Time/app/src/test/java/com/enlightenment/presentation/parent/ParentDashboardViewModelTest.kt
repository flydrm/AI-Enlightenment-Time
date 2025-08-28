package com.enlightenment.presentation.parent

import app.cash.turbine.test
import com.enlightenment.domain.repository.AchievementRepository
import com.enlightenment.domain.repository.UserProgressRepository
import com.enlightenment.domain.usecase.GetDailyProgressUseCase
import com.enlightenment.domain.usecase.GetWeeklyProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParentDashboardViewModelTest {
    
    @Mock
    private lateinit var mockUserProgressRepository: UserProgressRepository
    
    @Mock
    private lateinit var mockAchievementRepository: AchievementRepository
    
    @Mock
    private lateinit var mockGetDailyProgressUseCase: GetDailyProgressUseCase
    
    @Mock
    private lateinit var mockGetWeeklyProgressUseCase: GetWeeklyProgressUseCase
    
    private lateinit var viewModel: ParentDashboardViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `test initial state is loading`() = runTest {
        // Given
        setupMockResponses()
        
        // When
        viewModel = ParentDashboardViewModel(
            userProgressRepository = mockUserProgressRepository,
            achievementRepository = mockAchievementRepository,
            getDailyProgressUseCase = mockGetDailyProgressUseCase,
            getWeeklyProgressUseCase = mockGetWeeklyProgressUseCase
        )
        
        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.totalLearningTime)
            assertEquals(0f, initialState.weeklyProgress)
            assertEquals(0, initialState.dailyStreak)
            assertTrue(initialState.todayActivities.isEmpty())
        }
    }
    
    @Test
    fun `test loadDashboardData updates state correctly`() = runTest {
        // Given
        val expectedTotalTime = 180 // 3小时
        val expectedProgress = 0.75f // 75%
        val expectedStreak = 7
        
        `when`(mockUserProgressRepository.getTotalLearningTime())
            .thenReturn(Result.success(expectedTotalTime))
        
        `when`(mockGetWeeklyProgressUseCase.invoke())
            .thenReturn(Result.success(expectedProgress))
        
        `when`(mockUserProgressRepository.getDailyStreak())
            .thenReturn(Result.success(expectedStreak))
        
        `when`(mockGetDailyProgressUseCase.getTodayActivities())
            .thenReturn(Result.success(emptyList()))
        
        `when`(mockAchievementRepository.getAllAchievements())
            .thenReturn(Result.success(emptyList()))
        
        // When
        viewModel = ParentDashboardViewModel(
            userProgressRepository = mockUserProgressRepository,
            achievementRepository = mockAchievementRepository,
            getDailyProgressUseCase = mockGetDailyProgressUseCase,
            getWeeklyProgressUseCase = mockGetWeeklyProgressUseCase
        )
        
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expectedTotalTime, state.totalLearningTime)
            assertEquals(expectedProgress, state.weeklyProgress)
            assertEquals(expectedStreak, state.dailyStreak)
        }
    }
    
    @Test
    fun `test refreshData reloads all data`() = runTest {
        // Given
        setupMockResponses()
        
        viewModel = ParentDashboardViewModel(
            userProgressRepository = mockUserProgressRepository,
            achievementRepository = mockAchievementRepository,
            getDailyProgressUseCase = mockGetDailyProgressUseCase,
            getWeeklyProgressUseCase = mockGetWeeklyProgressUseCase
        )
        
        advanceUntilIdle()
        
        // When
        viewModel.refreshData()
        advanceUntilIdle()
        
        // Then
        verify(mockUserProgressRepository, times(2)).getTotalLearningTime()
        verify(mockGetWeeklyProgressUseCase, times(2)).invoke()
        verify(mockUserProgressRepository, times(2)).getDailyStreak()
    }
    
    @Test
    fun `test achievement progress calculation`() = runTest {
        // Given
        val achievements = listOf(
            createMockAchievement("成就1", true),
            createMockAchievement("成就2", true),
            createMockAchievement("成就3", false),
            createMockAchievement("成就4", false),
            createMockAchievement("成就5", true)
        )
        
        setupMockResponses()
        `when`(mockAchievementRepository.getAllAchievements())
            .thenReturn(Result.success(achievements))
        
        // When
        viewModel = ParentDashboardViewModel(
            userProgressRepository = mockUserProgressRepository,
            achievementRepository = mockAchievementRepository,
            getDailyProgressUseCase = mockGetDailyProgressUseCase,
            getWeeklyProgressUseCase = mockGetWeeklyProgressUseCase
        )
        
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.unlockedAchievements)
            assertEquals(5, state.totalAchievements)
            assertEquals(3, state.recentAchievements.size)
        }
    }
    
    private fun setupMockResponses() {
        `when`(mockUserProgressRepository.getTotalLearningTime())
            .thenReturn(Result.success(0))
        
        `when`(mockGetWeeklyProgressUseCase.invoke())
            .thenReturn(Result.success(0f))
        
        `when`(mockUserProgressRepository.getDailyStreak())
            .thenReturn(Result.success(0))
        
        `when`(mockGetDailyProgressUseCase.getTodayActivities())
            .thenReturn(Result.success(emptyList()))
        
        `when`(mockAchievementRepository.getAllAchievements())
            .thenReturn(Result.success(emptyList()))
    }
    
    private fun createMockAchievement(name: String, unlocked: Boolean): Any {
        // 这里应该返回实际的Achievement对象
        // 为了测试简化，使用mock
        return mock(Any::class.java).apply {
            `when`(this.toString()).thenReturn(name)
        }
    }
}