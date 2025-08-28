package com.enlightenment.presentation.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enlightenment.presentation.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    navController: NavController,
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "家长控制面板",
                        style = Typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 学习概览卡片
            item {
                LearningOverviewCard(
                    totalLearningTime = uiState.totalLearningTime,
                    weeklyProgress = uiState.weeklyProgress,
                    dailyStreak = uiState.dailyStreak
                )
            }
            
            // 快速操作卡片
            item {
                QuickActionsCard(
                    onViewReport = { navController.navigate("learning_report") },
                    onManageContent = { navController.navigate("content_management") },
                    onViewAchievements = { navController.navigate("achievements") },
                    onScheduleSettings = { navController.navigate("schedule_settings") }
                )
            }
            
            // 今日活动卡片
            item {
                TodayActivityCard(
                    activities = uiState.todayActivities
                )
            }
            
            // 成就进度卡片
            item {
                AchievementProgressCard(
                    unlockedCount = uiState.unlockedAchievements,
                    totalCount = uiState.totalAchievements,
                    recentAchievements = uiState.recentAchievements
                )
            }
            
            // 安全设置卡片
            item {
                SecuritySettingsCard(
                    onChangePin = { navController.navigate("change_pin") },
                    onViewAuditLog = { navController.navigate("audit_log") }
                )
            }
        }
    }
}

@Composable
fun LearningOverviewCard(
    totalLearningTime: Int,
    weeklyProgress: Float,
    dailyStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "学习概览",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = "${totalLearningTime / 60}小时${totalLearningTime % 60}分",
                    label = "总学习时长",
                    icon = Icons.Default.Timer
                )
                
                StatisticItem(
                    value = "${(weeklyProgress * 100).toInt()}%",
                    label = "本周完成度",
                    icon = Icons.Default.TrendingUp
                )
                
                StatisticItem(
                    value = "$dailyStreak 天",
                    label = "连续学习",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = Typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsCard(
    onViewReport: () -> Unit,
    onManageContent: () -> Unit,
    onViewAchievements: () -> Unit,
    onScheduleSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "快速操作",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "学习报告",
                    icon = Icons.Default.Assessment,
                    onClick = onViewReport,
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    text = "内容管理",
                    icon = Icons.Default.FolderOpen,
                    onClick = onManageContent,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "成就查看",
                    icon = Icons.Default.EmojiEvents,
                    onClick = onViewAchievements,
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    text = "时间设置",
                    icon = Icons.Default.Schedule,
                    onClick = onScheduleSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun TodayActivityCard(
    activities: List<Activity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "今日活动",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activities.isEmpty()) {
                Text(
                    "今天还没有学习活动",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activities.forEach { activity ->
                    ActivityItem(activity = activity)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (activity.type) {
                ActivityType.STORY -> Icons.Default.Book
                ActivityType.EXPLORATION -> Icons.Default.Explore
                ActivityType.ACHIEVEMENT -> Icons.Default.Star
                ActivityType.VOICE -> Icons.Default.Mic
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.time,
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${activity.duration}分钟",
            style = Typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementProgressCard(
    unlockedCount: Int,
    totalCount: Int,
    recentAchievements: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "成就进度",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "$unlockedCount / $totalCount",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = unlockedCount.toFloat() / totalCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            if (recentAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "最近解锁：",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                recentAchievements.forEach { achievement ->
                    Text(
                        "• $achievement",
                        style = Typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SecuritySettingsCard(
    onChangePin: () -> Unit,
    onViewAuditLog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "安全设置",
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onChangePin,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("修改PIN码")
                }
                
                OutlinedButton(
                    onClick = onViewAuditLog,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("审计日志")
                }
            }
        }
    }
}

// 数据类
data class Activity(
    val title: String,
    val time: String,
    val duration: Int,
    val type: ActivityType
)

enum class ActivityType {
    STORY,
    EXPLORATION,
    ACHIEVEMENT,
    VOICE
}