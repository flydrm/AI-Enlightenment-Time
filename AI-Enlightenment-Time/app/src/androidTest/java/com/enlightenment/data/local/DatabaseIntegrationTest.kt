package com.enlightenment.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enlightenment.data.local.dao.StoryDao
import com.enlightenment.data.local.dao.UserProgressDao
import com.enlightenment.data.local.database.AppDatabase
import com.enlightenment.data.local.entity.StoryEntity
import com.enlightenment.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var storyDao: StoryDao
    private lateinit var userProgressDao: UserProgressDao
    
    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        
        storyDao = database.storyDao()
        userProgressDao = database.userProgressDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }
    
    @Test
    fun testInsertAndRetrieveStory() = runTest {
        // Given
        val story = StoryEntity(
            title = "测试故事",
            content = "这是一个测试故事的内容",
            category = "冒险",
            ageGroup = 5,
            imageUrl = "https://example.com/image.jpg",
            audioUrl = null,
            createdAt = System.currentTimeMillis(),
            isFavorite = false,
            readCount = 0
        )
        
        // When
        val id = storyDao.insertStory(story)
        val retrievedStory = storyDao.getStoryById(id)
        
        // Then
        assertNotNull(retrievedStory)
        assertEquals(story.title, retrievedStory.title)
        assertEquals(story.content, retrievedStory.content)
        assertEquals(story.category, retrievedStory.category)
    }
    
    @Test
    fun testGetStoriesByCategory() = runTest {
        // Given
        val stories = listOf(
            createStoryEntity("故事1", "冒险"),
            createStoryEntity("故事2", "科学"),
            createStoryEntity("故事3", "冒险"),
            createStoryEntity("故事4", "童话")
        )
        
        stories.forEach { storyDao.insertStory(it) }
        
        // When
        val adventureStories = storyDao.getStoriesByCategory("冒险").first()
        
        // Then
        assertEquals(2, adventureStories.size)
        assertTrue(adventureStories.all { it.category == "冒险" })
    }
    
    @Test
    fun testUpdateUserProgress() = runTest {
        // Given
        val date = LocalDate.now()
        val initialProgress = UserProgressEntity(
            date = date,
            totalMinutes = 15,
            storiesCompleted = 1,
            achievementsUnlocked = 0,
            skillPoints = 150
        )
        
        userProgressDao.insertProgress(initialProgress)
        
        // When
        val updatedProgress = initialProgress.copy(
            totalMinutes = 30,
            storiesCompleted = 2,
            skillPoints = 300
        )
        userProgressDao.updateProgress(updatedProgress)
        
        val retrievedProgress = userProgressDao.getProgressByDate(date)
        
        // Then
        assertNotNull(retrievedProgress)
        assertEquals(30, retrievedProgress.totalMinutes)
        assertEquals(2, retrievedProgress.storiesCompleted)
        assertEquals(300, retrievedProgress.skillPoints)
    }
    
    @Test
    fun testGetProgressBetweenDates() = runTest {
        // Given
        val today = LocalDate.now()
        val progressList = listOf(
            createProgressEntity(today),
            createProgressEntity(today.minusDays(1)),
            createProgressEntity(today.minusDays(2)),
            createProgressEntity(today.minusDays(3)),
            createProgressEntity(today.minusDays(7)) // 超出范围
        )
        
        progressList.forEach { userProgressDao.insertProgress(it) }
        
        // When
        val weekProgress = userProgressDao.getProgressBetweenDates(
            today.minusDays(3),
            today
        ).first()
        
        // Then
        assertEquals(4, weekProgress.size)
        assertTrue(weekProgress.none { it.date == today.minusDays(7) })
    }
    
    @Test
    fun testFavoriteStoriesFlow() = runTest {
        // Given
        val stories = listOf(
            createStoryEntity("故事1", "冒险", isFavorite = true),
            createStoryEntity("故事2", "科学", isFavorite = false),
            createStoryEntity("故事3", "童话", isFavorite = true)
        )
        
        val storyIds = stories.map { storyDao.insertStory(it) }
        
        // When
        val favoriteStories = storyDao.getFavoriteStories().first()
        
        // Then
        assertEquals(2, favoriteStories.size)
        assertTrue(favoriteStories.all { it.isFavorite })
        
        // When - 更新收藏状态
        storyDao.updateFavoriteStatus(storyIds[1], true)
        val updatedFavorites = storyDao.getFavoriteStories().first()
        
        // Then
        assertEquals(3, updatedFavorites.size)
    }
    
    @Test
    fun testTransactionSupport() = runTest {
        // Given
        val story = createStoryEntity("事务测试故事", "测试")
        val progress = createProgressEntity(LocalDate.now())
        
        // When - 在事务中执行多个操作
        database.runInTransaction {
            runTest {
                storyDao.insertStory(story)
                userProgressDao.insertProgress(progress)
            }
        }
        
        // Then
        val stories = storyDao.getAllStories().first()
        val todayProgress = userProgressDao.getProgressByDate(LocalDate.now())
        
        assertTrue(stories.isNotEmpty())
        assertNotNull(todayProgress)
    }
    
    private fun createStoryEntity(
        title: String,
        category: String,
        isFavorite: Boolean = false
    ): StoryEntity {
        return StoryEntity(
            title = title,
            content = "内容：$title",
            category = category,
            ageGroup = 5,
            imageUrl = "",
            audioUrl = null,
            createdAt = System.currentTimeMillis(),
            isFavorite = isFavorite,
            readCount = 0
        )
    }
    
    private fun createProgressEntity(date: LocalDate): UserProgressEntity {
        return UserProgressEntity(
            date = date,
            totalMinutes = 15,
            storiesCompleted = 1,
            achievementsUnlocked = 0,
            skillPoints = 150
        )
    }
}