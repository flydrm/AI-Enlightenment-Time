package com.enlightenment.presentation.parent

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.format.DateTimeFormatter
import java.time.LocalDate





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningReportScreen(
    navController: NavController,
    viewModel: LearningReportViewModel = remember { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "学习报告",
                        style = MaterialTheme.typography.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportReport() }) {
                        Icon(Icons.Default.Share, contentDescription = "分享报告")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 时间选择标签
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        viewModel.loadWeeklyReport()
                    },
                    text = { Text("本周") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        viewModel.loadMonthlyReport()
                    },
                    text = { Text("本月") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        viewModel.loadAllTimeReport()
                    },
                    text = { Text("全部") }
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 学习时长统计
                item {
                    LearningTimeCard(
                        totalMinutes = uiState.totalLearningMinutes,
                        averageMinutesPerDay = uiState.averageMinutesPerDay,
                        learningDays = uiState.learningDays
                    )
                }
                
                // 学习趋势图表
                item {
                    LearningTrendCard(
                        trendData = uiState.learningTrend
                    )
                }
                
                // 内容分布
                item {
                    ContentDistributionCard(
                        distribution = uiState.contentDistribution
                    )
                }
                
                // 技能进展
                item {
                    SkillProgressCard(
                        skills = uiState.skillProgress
                    )
                }
                
                // 详细活动列表
                item {
                    Text(
                        "学习活动详情",
                        style = MaterialTheme.typography.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.detailedActivities) { activity ->
                    DetailedActivityCard(activity = activity)
                }
            }
        }
    }
}
@Composable
fun LearningTimeCard(
    totalMinutes: Int,
    averageMinutesPerDay: Int,
    learningDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "学习时长统计",
                style = MaterialTheme.typography.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeStatItem(
                    value = "${totalMinutes / 60}小时${totalMinutes % 60}分",
                    label = "总学习时长",
                    color = MaterialTheme.colorScheme.primary
                )
                
                TimeStatItem(
                    value = "$averageMinutesPerDay 分钟",
                    label = "日均学习",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                TimeStatItem(
                    value = "$learningDays 天",
                    label = "学习天数",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
@Composable
fun TimeStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun LearningTrendCard(
    trendData: List<DailyLearningData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "学习趋势",
                style = MaterialTheme.typography.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (trendData.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    drawLearningTrendChart(trendData)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 图表说明
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "最近${trendData.size}天学习时长变化",
                        style = MaterialTheme.typography.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "暂无数据",
                    style = MaterialTheme.typography.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
fun DrawScope.drawLearningTrendChart(data: List<DailyLearningData>) {
    if (data.isEmpty()) return
    
    val maxMinutes = data.maxOf { it.minutes }.coerceAtLeast(1)
    val xStep = size.width / (data.size - 1).coerceAtLeast(1)
    val yScale = size.height * 0.8f / maxMinutes
    
    val path = Path()
    val points = data.mapIndexed { index, item ->
        Offset(
            x = index * xStep,
            y = size.height - (item.minutes * yScale) - size.height * 0.1f
        )
    }
    
    // 绘制曲线
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        val previousPoint = points[i - 1]
        val currentPoint = points[i]
        val controlPoint1 = Offset(
            (previousPoint.x + currentPoint.x) / 2,
            previousPoint.y
        )
        val controlPoint2 = Offset(
            (previousPoint.x + currentPoint.x) / 2,
            currentPoint.y
        )
        path.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            currentPoint.x, currentPoint.y
        )
    }
    
    drawPath(
        path = path,
        color = Color(0xFF4CAF50),
        style = Stroke(width = 3.dp.toPx())
    )
    
    // 绘制数据点
    points.forEach { point ->
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 4.dp.toPx(),
            center = point
        )
    }
}
@Composable
fun ContentDistributionCard(
    distribution: Map<String, Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "内容分布",
                style = MaterialTheme.typography.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            distribution.forEach { (category, percentage) ->
                ContentCategoryItem(
                    category = category,
                    percentage = percentage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
@Composable
fun ContentCategoryItem(
    category: String,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.typography.bodyMedium
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
@Composable
fun SkillProgressCard(
    skills: List<SkillProgress>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "技能进展",
                style = MaterialTheme.typography.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            skills.forEach { skill ->
                SkillProgressItem(skill = skill)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
@Composable
fun SkillProgressItem(skill: SkillProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (skill.name) {
                "语言表达" -> Icons.Default.RecordVoiceOver
                "逻辑思维" -> Icons.Default.Psychology
                "创造力" -> Icons.Default.Palette
                "观察力" -> Icons.Default.RemoveRedEye
                else -> Icons.Default.School
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.typography.bodyMedium
                )
                Text(
                    text = "Lv.${skill.level}",
                    style = MaterialTheme.typography.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = skill.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}
@Composable
fun DetailedActivityCard(activity: DetailedActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (activity.type) {
                    "story" -> Icons.Default.Book
                    "exploration" -> Icons.Default.Explore
                    "achievement" -> Icons.Default.Star
                    else -> Icons.Default.PlayCircle
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.date.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm")),
                    style = MaterialTheme.typography.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (activity.description.isNotEmpty()) {
                    Text(
                        text = activity.description,
                        style = MaterialTheme.typography.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${activity.duration}分钟",
                    style = MaterialTheme.typography.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                if (activity.score > 0) {
                    Text(
                        text = "+${activity.score}分",
                        style = MaterialTheme.typography.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
// 数据类
data class DailyLearningData(
    val date: LocalDate,
    val minutes: Int
)
data class SkillProgress(
    val name: String,
    val level: Int,
    val progress: Float // 0-1
)
data class DetailedActivity(
    val title: String,
    val type: String,
    val date: LocalDate,
    val duration: Int,
    val score: Int,
    val description: String
)
