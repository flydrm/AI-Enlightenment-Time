package com.enlightenment.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enlightenment.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun testMainScreenDisplayed() {
        // 验证主屏幕显示
        composeTestRule
            .onNodeWithText("AI启蒙时光")
            .assertIsDisplayed()
    }
    
    @Test
    fun testNavigationToStoryScreen() {
        // 点击开始探索按钮
        composeTestRule
            .onNodeWithText("开始探索")
            .performClick()
        
        // 验证进入故事界面
        composeTestRule
            .onNodeWithText("选择一个主题")
            .assertIsDisplayed()
    }
    
    @Test
    fun testParentAuthenticationFlow() {
        // 点击家长入口
        composeTestRule
            .onNodeWithContentDescription("家长入口")
            .performClick()
        
        // 验证显示PIN输入界面
        composeTestRule
            .onNodeWithText("请输入家长PIN码")
            .assertIsDisplayed()
        
        // 输入PIN码
        composeTestRule
            .onNodeWithTag("pin_input")
            .performTextInput("1234")
        
        // 点击确认
        composeTestRule
            .onNodeWithText("确认")
            .performClick()
        
        // 验证数学题验证
        composeTestRule
            .onNodeWithText("请回答数学题")
            .assertIsDisplayed()
    }
    
    @Test
    fun testCameraFeature() {
        // 导航到相机功能
        composeTestRule
            .onNodeWithContentDescription("拍照识别")
            .performClick()
        
        // 验证相机界面显示
        composeTestRule
            .onNodeWithText("拍照探索")
            .assertIsDisplayed()
        
        // 验证相机权限提示
        composeTestRule
            .onNodeWithText("需要相机权限")
            .assertExists()
    }
    
    @Test
    fun testAchievementDisplay() {
        // 导航到成就界面
        composeTestRule
            .onNodeWithContentDescription("成就")
            .performClick()
        
        // 验证成就界面显示
        composeTestRule
            .onNodeWithText("我的成就")
            .assertIsDisplayed()
        
        // 验证成就列表存在
        composeTestRule
            .onNodeWithTag("achievement_list")
            .assertExists()
    }
    
    @Test
    fun testVoiceInteraction() {
        // 点击语音按钮
        composeTestRule
            .onNodeWithContentDescription("语音互动")
            .performClick()
        
        // 验证语音界面显示
        composeTestRule
            .onNodeWithText("点击麦克风开始说话")
            .assertIsDisplayed()
        
        // 验证麦克风按钮存在
        composeTestRule
            .onNodeWithTag("mic_button")
            .assertExists()
            .assertHasClickAction()
    }
    
    @Test
    fun testOfflineModeIndicator() {
        // 模拟离线状态
        // 这需要配合测试环境设置
        
        // 验证离线提示显示
        composeTestRule
            .onNodeWithText("离线模式")
            .assertExists()
    }
    
    @Test
    fun testSettingsNavigation() {
        // 进入家长门户
        authenticateAsParent()
        
        // 点击设置
        composeTestRule
            .onNodeWithContentDescription("设置")
            .performClick()
        
        // 验证设置界面显示
        composeTestRule
            .onNodeWithText("设置")
            .assertIsDisplayed()
        
        // 验证设置项存在
        composeTestRule
            .onNodeWithText("每日学习时长")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("学习提醒时间")
            .assertExists()
    }
    
    @Test
    fun testDailyLearningFlow() {
        // 开始学习
        composeTestRule
            .onNodeWithText("开始今日学习")
            .performClick()
        
        // 选择故事类型
        composeTestRule
            .onNodeWithText("冒险故事")
            .performClick()
        
        // 等待故事生成
        composeTestRule.waitForIdle()
        
        // 验证故事显示
        composeTestRule
            .onNodeWithTag("story_content")
            .assertExists()
        
        // 完成故事
        composeTestRule
            .onNodeWithText("完成")
            .performClick()
        
        // 验证积分增加提示
        composeTestRule
            .onNodeWithText("+100 积分")
            .assertIsDisplayed()
    }
    
    private fun authenticateAsParent() {
        // 辅助方法：完成家长认证
        composeTestRule
            .onNodeWithContentDescription("家长入口")
            .performClick()
        
        composeTestRule
            .onNodeWithTag("pin_input")
            .performTextInput("1234")
        
        composeTestRule
            .onNodeWithText("确认")
            .performClick()
        
        // 假设数学题答案
        composeTestRule
            .onNodeWithTag("math_answer_input")
            .performTextInput("10")
        
        composeTestRule
            .onNodeWithText("验证")
            .performClick()
    }
}