package com.enlightenment.presentation.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enlightenment.presentation.components.AnimatedPanda
import com.enlightenment.presentation.navigation.Screen
import com.enlightenment.presentation.ui.responsive.*

/**
 * 响应式主页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveHomeScreen(
    navController: NavController
) {
    val responsiveSizes = rememberResponsiveSizes()
    
    when (responsiveSizes.windowSizeClass) {
        WindowSizeClass.COMPACT -> CompactHomeLayout(navController)
        WindowSizeClass.MEDIUM -> MediumHomeLayout(navController)
        WindowSizeClass.EXPANDED -> ExpandedHomeLayout(navController)
    }
}

/**
 * 紧凑布局（手机）
 */
@Composable
private fun CompactHomeLayout(navController: NavController) {
    val horizontalPadding = ResponsivePadding.horizontal()
    val verticalPadding = ResponsivePadding.vertical()
    
    Scaffold(
        topBar = {
            CompactTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 小熊猫欢迎动画
            AnimatedPanda(
                modifier = Modifier
                    .size(120.dp)
                    .padding(vertical = verticalPadding),
                speech = "欢迎来到AI启蒙时光！"
            )
            
            // 功能网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(getMenuItems()) { item ->
                    CompactMenuItem(
                        item = item,
                        onClick = { navigateToScreen(navController, item) }
                    )
                }
            }
        }
    }
}

/**
 * 中等布局（平板竖屏）
 */
@Composable
private fun MediumHomeLayout(navController: NavController) {
    val horizontalPadding = ResponsivePadding.horizontal()
    val verticalPadding = ResponsivePadding.vertical()
    
    Scaffold(
        topBar = {
            MediumTopBar()
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizontalPadding)
        ) {
            // 左侧：小熊猫和欢迎信息
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedPanda(
                    modifier = Modifier.size(180.dp),
                    speech = "选择你想玩的项目吧！"
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "今天想学什么呢？",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
            
            // 右侧：功能网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(start = horizontalPadding)
            ) {
                items(getMenuItems()) { item ->
                    MediumMenuItem(
                        item = item,
                        onClick = { navigateToScreen(navController, item) }
                    )
                }
            }
        }
    }
}

/**
 * 展开布局（平板横屏/桌面）
 */
@Composable
private fun ExpandedHomeLayout(navController: NavController) {
    val horizontalPadding = ResponsivePadding.horizontal()
    val verticalPadding = ResponsivePadding.vertical()
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 侧边导航栏
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            AnimatedPanda(
                modifier = Modifier.size(80.dp),
                isActive = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            getMenuItems().forEach { item ->
                NavigationRailItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = false,
                    onClick = { navigateToScreen(navController, item) }
                )
            }
        }
        
        // 主内容区
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
        ) {
            // 顶部欢迎区
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = verticalPadding),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "欢迎回来！",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text = "今天想和小熊猫乐乐一起探索什么呢？",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    AnimatedPanda(
                        modifier = Modifier.size(120.dp),
                        speech = "我们开始吧！"
                    )
                }
            }
            
            // 功能卡片网格
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                contentPadding = PaddingValues(vertical = verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(getMenuItems()) { item ->
                    ExpandedMenuItem(
                        item = item,
                        onClick = { navigateToScreen(navController, item) }
                    )
                }
            }
        }
    }
}

/**
 * 紧凑布局的菜单项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactMenuItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = item.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = item.iconColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 中等布局的菜单项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediumMenuItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = item.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(56.dp),
                tint = item.iconColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 展开布局的菜单项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedMenuItem(
    item: MenuItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = item.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(64.dp),
                tint = item.iconColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactTopBar() {
    TopAppBar(
        title = { Text("AI启蒙时光") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediumTopBar() {
    CenterAlignedTopAppBar(
        title = { 
            Text(
                "AI启蒙时光",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * 菜单项数据
 */
private data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val iconColor: androidx.compose.ui.graphics.Color,
    val screen: Screen
)

/**
 * 获取菜单项列表
 */
@Composable
private fun getMenuItems(): List<MenuItem> {
    val colors = MaterialTheme.colorScheme
    
    return remember {
        listOf(
            MenuItem(
                title = "故事世界",
                description = "听精彩的故事",
                icon = Icons.Default.MenuBook,
                backgroundColor = colors.primaryContainer,
                iconColor = colors.onPrimaryContainer,
                screen = Screen.Story
            ),
            MenuItem(
                title = "拍照识物",
                description = "认识新事物",
                icon = Icons.Default.PhotoCamera,
                backgroundColor = colors.secondaryContainer,
                iconColor = colors.onSecondaryContainer,
                screen = Screen.Camera
            ),
            MenuItem(
                title = "语音对话",
                description = "和小熊猫聊天",
                icon = Icons.Default.Mic,
                backgroundColor = colors.tertiaryContainer,
                iconColor = colors.onTertiaryContainer,
                screen = Screen.Voice
            ),
            MenuItem(
                title = "成就勋章",
                description = "查看学习成果",
                icon = Icons.Default.EmojiEvents,
                backgroundColor = colors.errorContainer,
                iconColor = colors.onErrorContainer,
                screen = Screen.Achievement
            )
        )
    }
}

/**
 * 导航到指定界面
 */
private fun navigateToScreen(navController: NavController, item: MenuItem) {
    navController.navigate(item.screen.route)
}