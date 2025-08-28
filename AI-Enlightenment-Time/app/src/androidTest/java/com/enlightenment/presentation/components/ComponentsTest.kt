package com.enlightenment.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enlightenment.presentation.parent.ParentDashboardScreen
import com.enlightenment.presentation.parent.LearningReportScreen
import com.enlightenment.presentation.settings.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testParentDashboardComponents() {
        // 设置测试内容
        composeTestRule.setContent {
            ParentDashboardScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 验证各个卡片显示
        composeTestRule
            .onNodeWithText("学习概览")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("快速操作")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("今日活动")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("成就进度")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("安全设置")
            .assertIsDisplayed()
    }
    
    @Test
    fun testLearningReportTabs() {
        composeTestRule.setContent {
            LearningReportScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 验证标签存在
        composeTestRule
            .onNodeWithText("本周")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        composeTestRule
            .onNodeWithText("本月")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        composeTestRule
            .onNodeWithText("全部")
            .assertIsDisplayed()
            .assertHasClickAction()
        
        // 点击本月标签
        composeTestRule
            .onNodeWithText("本月")
            .performClick()
        
        // 验证内容更新
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun testSettingsScreenSections() {
        composeTestRule.setContent {
            SettingsScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 验证各个设置部分
        val sections = listOf(
            "学习设置",
            "内容设置",
            "AI设置",
            "隐私与安全",
            "通用设置",
            "关于",
            "数据管理"
        )
        
        sections.forEach { section ->
            composeTestRule
                .onNodeWithText(section)
                .assertIsDisplayed()
        }
    }
    
    @Test
    fun testSettingsSwitchInteraction() {
        composeTestRule.setContent {
            SettingsScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 找到内容过滤开关
        composeTestRule
            .onNode(
                hasText("内容过滤") and hasAnyDescendant(hasTestTag("switch"))
            )
            .assertExists()
        
        // 点击开关
        composeTestRule
            .onAllNodes(hasContentDescription("Switch"))
            .filterToOne(hasAnyAncestor(hasText("内容过滤")))
            .performClick()
        
        // 验证状态改变
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun testQuickActionsNavigation() {
        composeTestRule.setContent {
            ParentDashboardScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 测试快速操作按钮
        val quickActions = listOf(
            "学习报告",
            "内容管理",
            "成就查看",
            "时间设置"
        )
        
        quickActions.forEach { action ->
            composeTestRule
                .onNodeWithText(action)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }
    
    @Test
    fun testLearningTimeCard() {
        composeTestRule.setContent {
            LearningReportScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 验证学习时长统计卡片
        composeTestRule
            .onNodeWithText("学习时长统计")
            .assertIsDisplayed()
        
        // 验证统计项
        composeTestRule
            .onNodeWithText("总学习时长")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("日均学习")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("学习天数")
            .assertIsDisplayed()
    }
    
    @Test
    fun testScrollingBehavior() {
        composeTestRule.setContent {
            SettingsScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 滚动到底部
        composeTestRule
            .onNodeWithText("重置所有设置")
            .performScrollTo()
            .assertIsDisplayed()
        
        // 滚动回顶部
        composeTestRule
            .onNodeWithText("学习设置")
            .performScrollTo()
            .assertIsDisplayed()
    }
    
    @Test
    fun testDialogInteraction() {
        composeTestRule.setContent {
            SettingsScreen(
                navController = androidx.navigation.compose.rememberNavController()
            )
        }
        
        // 点击重置设置
        composeTestRule
            .onNodeWithText("重置所有设置")
            .performScrollTo()
            .performClick()
        
        // 验证对话框显示
        composeTestRule
            .onNodeWithText("确定要重置所有设置吗？此操作不可撤销。")
            .assertIsDisplayed()
        
        // 点击取消
        composeTestRule
            .onNodeWithText("取消")
            .performClick()
        
        // 验证对话框关闭
        composeTestRule
            .onNodeWithText("确定要重置所有设置吗？此操作不可撤销。")
            .assertDoesNotExist()
    }
}