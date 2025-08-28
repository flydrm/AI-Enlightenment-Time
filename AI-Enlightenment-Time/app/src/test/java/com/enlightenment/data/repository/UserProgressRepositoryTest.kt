package com.enlightenment.data.repository

import com.enlightenment.data.local.dao.UserProgressDao
import com.enlightenment.data.local.entity.UserProgressEntity
import com.enlightenment.domain.repository.UserProgressRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserProgressRepositoryTest {
    
    @Mock
    private lateinit var mockUserProgressDao: UserProgressDao
    
    private lateinit var repository: UserProgressRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = UserProgressRepositoryImpl(mockUserProgressDao)
    }
    
    @Test
    fun `test getTotalLearningTime returns sum of all learning time`() = runTest {
        // Given
        val expectedTotal = 300 // 5小时
        `when`(mockUserProgressDao.getTotalLearningMinutes())
            .thenReturn(expectedTotal)
        
        // When
        val result = repository.getTotalLearningTime()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedTotal, result.getOrNull())
        verify(mockUserProgressDao).getTotalLearningMinutes()
    }
    
    @Test
    fun `test getDailyStreak calculates consecutive days correctly`() = runTest {
        // Given
        val today = LocalDate.now()
        val progressList = listOf(
            createProgressEntity(today, 15),
            createProgressEntity(today.minusDays(1), 20),
            createProgressEntity(today.minusDays(2), 25),
            // 跳过一天
            createProgressEntity(today.minusDays(4), 15)
        )
        
        `when`(mockUserProgressDao.getAllProgress())
            .thenReturn(flowOf(progressList))
        
        // When
        val result = repository.getDailyStreak()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()) // 连续3天
    }
    
    @Test
    fun `test updateDailyProgress creates or updates progress`() = runTest {
        // Given
        val date = LocalDate.now()
        val minutes = 15
        val existingProgress = createProgressEntity(date, 10)
        
        `when`(mockUserProgressDao.getProgressByDate(date))
            .thenReturn(existingProgress)
        
        // When
        val result = repository.updateDailyProgress(date, minutes)
        
        // Then
        assertTrue(result.isSuccess)
        verify(mockUserProgressDao).updateProgress(any())
    }
    
    @Test
    fun `test getWeeklyProgress calculates percentage correctly`() = runTest {
        // Given
        val today = LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val progressList = listOf(
            createProgressEntity(weekStart, 15),
            createProgressEntity(weekStart.plusDays(1), 15),
            createProgressEntity(weekStart.plusDays(2), 15),
            createProgressEntity(weekStart.plusDays(3), 0),
            createProgressEntity(weekStart.plusDays(4), 15)
        )
        
        `when`(mockUserProgressDao.getProgressBetweenDates(any(), any()))
            .thenReturn(flowOf(progressList))
        
        // When
        val result = repository.getWeeklyProgress()
        
        // Then
        assertTrue(result.isSuccess)
        val progress = result.getOrNull()!!
        assertTrue(progress > 0.5f) // 应该超过50%
    }
    
    @Test
    fun `test getSkillProgress returns skill levels`() = runTest {
        // Given
        val skills = listOf(
            createSkillProgress("语言表达", 3, 0.7f),
            createSkillProgress("逻辑思维", 2, 0.4f),
            createSkillProgress("创造力", 4, 0.9f)
        )
        
        `when`(mockUserProgressDao.getAllSkillProgress())
            .thenReturn(flowOf(skills))
        
        // When
        val result = repository.getSkillProgress()
        
        // Then
        assertEquals(3, result.size)
        assertEquals("语言表达", result[0].name)
        assertEquals(3, result[0].level)
    }
    
    @Test
    fun `test updateSkillProgress increments experience`() = runTest {
        // Given
        val skillName = "逻辑思维"
        val experienceGain = 50
        val currentSkill = createSkillProgress(skillName, 2, 0.6f)
        
        `when`(mockUserProgressDao.getSkillProgress(skillName))
            .thenReturn(currentSkill)
        
        // When
        val result = repository.updateSkillProgress(skillName, experienceGain)
        
        // Then
        assertTrue(result.isSuccess)
        verify(mockUserProgressDao).updateSkillProgress(any())
    }
    
    private fun createProgressEntity(date: LocalDate, minutes: Int): UserProgressEntity {
        return UserProgressEntity(
            id = 0,
            date = date,
            totalMinutes = minutes,
            storiesCompleted = if (minutes > 0) 1 else 0,
            achievementsUnlocked = 0,
            skillPoints = minutes * 10
        )
    }
    
    private fun createSkillProgress(name: String, level: Int, progress: Float): Any {
        // 返回实际的SkillProgress实体
        return mock(Any::class.java)
    }
}