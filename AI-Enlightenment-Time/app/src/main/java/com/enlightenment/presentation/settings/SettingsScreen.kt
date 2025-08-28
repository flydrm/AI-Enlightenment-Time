package com.enlightenment.presentation.settings

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = remember { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var selectedApiKey by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "设置",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 学习设置
            item {
                SettingsSection(title = "学习设置") {
                    // 每日学习时长
                    SettingsItem(
                        icon = Icons.Default.Timer,
                        title = "每日学习时长",
                        subtitle = "${uiState.dailyLearningMinutes} 分钟",
                        onClick = { viewModel.showDailyTimeDialog() }
                    )
                    
                    // 学习提醒时间
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "学习提醒时间",
                        subtitle = uiState.reminderTime,
                        onClick = { viewModel.showReminderTimeDialog() }
                    )
                    
                    // 休息日设置
                    SettingsItem(
                        icon = Icons.Default.Weekend,
                        title = "休息日",
                        subtitle = if (uiState.restDays.isEmpty()) "无" else uiState.restDays.joinToString(", "),
                        onClick = { viewModel.showRestDaysDialog() }
                    )
                }
            }
            
            // 内容设置
            item {
                SettingsSection(title = "内容设置") {
                    // 难度级别
                    SettingsItem(
                        icon = Icons.Default.SignalCellularAlt,
                        title = "难度级别",
                        subtitle = uiState.difficultyLevel,
                        onClick = { viewModel.showDifficultyDialog() }
                    )
                    
                    // 内容过滤
                    SettingsItemSwitch(
                        icon = Icons.Default.FilterAlt,
                        title = "内容过滤",
                        subtitle = "过滤不适合的内容",
                        checked = uiState.contentFilterEnabled,
                        onCheckedChange = { viewModel.toggleContentFilter() }
                    )
                    
                    // 兴趣标签
                    SettingsItem(
                        icon = Icons.Default.Label,
                        title = "兴趣标签",
                        subtitle = if (uiState.interestTags.isEmpty()) "未设置" else "${uiState.interestTags.size} 个标签",
                        onClick = { navController.navigate("interest_tags") }
                    )
                }
            }
            
            // AI设置
            item {
                SettingsSection(title = "AI设置") {
                    // AI模型选择
                    SettingsItem(
                        icon = Icons.Default.SmartToy,
                        title = "AI模型偏好",
                        subtitle = uiState.preferredAIModel,
                        onClick = { viewModel.showAIModelDialog() }
                    )
                    
                    // API密钥管理
                    SettingsItem(
                        icon = Icons.Default.Key,
                        title = "API密钥管理",
                        subtitle = "管理AI服务密钥",
                        onClick = { navController.navigate("api_keys") }
                    )
                    
                    // 离线模式
                    SettingsItemSwitch(
                        icon = Icons.Default.CloudOff,
                        title = "优先离线模式",
                        subtitle = "网络不佳时自动切换",
                        checked = uiState.offlineModeEnabled,
                        onCheckedChange = { viewModel.toggleOfflineMode() }
                    )
                }
            }
            
            // 隐私与安全
            item {
                SettingsSection(title = "隐私与安全") {
                    // 数据收集
                    SettingsItemSwitch(
                        icon = Icons.Default.Analytics,
                        title = "使用数据分析",
                        subtitle = "帮助改善应用体验",
                        checked = uiState.analyticsEnabled,
                        onCheckedChange = { viewModel.toggleAnalytics() }
                    )
                    
                    // 数据备份
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "数据备份",
                        subtitle = "上次备份: ${uiState.lastBackupDate}",
                        onClick = { viewModel.backupData() }
                    )
                    
                    // 审计日志
                    SettingsItem(
                        icon = Icons.Default.History,
                        title = "审计日志",
                        subtitle = "查看所有操作记录",
                        onClick = { navController.navigate("audit_log") }
                    )
                }
            }
            
            // 通用设置
            item {
                SettingsSection(title = "通用设置") {
                    // 语言
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "语言",
                        subtitle = uiState.language,
                        onClick = { viewModel.showLanguageDialog() }
                    )
                    
                    // 主题
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题",
                        subtitle = uiState.theme,
                        onClick = { viewModel.showThemeDialog() }
                    )
                    
                    // 音效
                    SettingsItemSwitch(
                        icon = Icons.Default.VolumeUp,
                        title = "音效",
                        subtitle = "应用内音效",
                        checked = uiState.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound() }
                    )
                }
            }
            
            // 关于
            item {
                SettingsSection(title = "关于") {
                    // 版本信息
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "版本",
                        subtitle = "v${uiState.appVersion}",
                        onClick = { }
                    )
                    
                    // 用户反馈
                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "用户反馈",
                        subtitle = "提交建议或问题",
                        onClick = { navController.navigate("feedback") }
                    )
                    
                    // 使用条款
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "使用条款",
                        subtitle = "查看服务条款",
                        onClick = { navController.navigate("terms") }
                    )
                    
                    // 隐私政策
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "隐私政策",
                        subtitle = "了解数据使用方式",
                        onClick = { navController.navigate("privacy") }
                    )
                }
            }
            
            // 危险区域
            item {
                SettingsSection(title = "数据管理") {
                    // 清除缓存
                    SettingsItem(
                        icon = Icons.Default.CleaningServices,
                        title = "清除缓存",
                        subtitle = "缓存大小: ${uiState.cacheSize}",
                        onClick = { viewModel.clearCache() }
                    )
                    
                    // 重置设置
                    SettingsItem(
                        icon = Icons.Default.RestartAlt,
                        title = "重置所有设置",
                        subtitle = "恢复默认设置",
                        onClick = { showResetDialog = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // 底部空间
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // 重置确认对话框
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置设置") },
            text = { Text("确定要重置所有设置吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 处理各种对话框
    uiState.currentDialog?.let { dialog ->
        when (dialog) {
            is SettingsDialog.DailyTime -> {
                DailyTimeDialog(
                    currentMinutes = uiState.dailyLearningMinutes,
                    onConfirm = { minutes ->
                        viewModel.updateDailyLearningTime(minutes)
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
            is SettingsDialog.ReminderTime -> {
                ReminderTimeDialog(
                    currentTime = uiState.reminderTime,
                    onConfirm = { time ->
                        viewModel.updateReminderTime(time)
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
            // 其他对话框...
            else -> {}
        }
    }
}
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
@Composable
fun DailyTimeDialog(
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(currentMinutes) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置每日学习时长") },
        text = {
            Column {
                Text("建议每日学习15-30分钟")
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = selectedMinutes.toFloat(),
                    onValueChange = { selectedMinutes = it.toInt() },
                    valueRange = 5f..60f,
                    steps = 10
                )
                
                Text(
                    text = "$selectedMinutes 分钟",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMinutes) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
@Composable
fun ReminderTimeDialog(
    currentTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 时间选择器实现
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置提醒时间") },
        text = {
            // 这里应该使用时间选择器组件
            Text("时间选择器")
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentTime) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
