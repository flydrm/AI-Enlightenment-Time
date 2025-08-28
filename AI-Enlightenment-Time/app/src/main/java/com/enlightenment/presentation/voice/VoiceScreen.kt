package com.enlightenment.presentation.voice

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.presentation.components.AnimatedPanda
import com.enlightenment.presentation.components.PermissionHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * 语音对话界面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    when {
        audioPermissionState.status.isGranted -> {
            VoiceContent(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
        else -> {
            PermissionHandler(
                permission = Manifest.permission.RECORD_AUDIO,
                rationale = "小熊猫想要听听你说话，需要使用麦克风权限哦！",
                onPermissionGranted = {
                    // 权限授予后会自动重组
                },
                onPermissionDenied = {
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
private fun VoiceContent(
    viewModel: VoiceViewModel,
    onNavigateBack: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val audioAmplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 当有新消息时滚动到底部
    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) {
            listState.animateScrollToItem(conversation.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部栏
            TopAppBar(
                title = { Text("和小熊猫聊天") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            // 对话列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(conversation) { message ->
                    MessageBubble(message = message)
                }
                
                // 显示当前状态
                if (voiceState is VoiceState.Processing) {
                    item {
                        ProcessingIndicator()
                    }
                }
            }
            
            // 底部控制区
            BottomVoiceControls(
                isListening = isListening,
                audioAmplitude = audioAmplitude,
                voiceState = voiceState,
                onStartListening = viewModel::startListening,
                onStopListening = viewModel::stopListening,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // 小熊猫助手（显示在右上角）
        AnimatedPanda(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .size(80.dp),
            isActive = isListening,
            speech = when (voiceState) {
                is VoiceState.Listening -> "我在听呢..."
                is VoiceState.Processing -> "让我想想..."
                is VoiceState.Speaking -> "听我说..."
                is VoiceState.Error -> "出错了，再试一次吧！"
                else -> null
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ConversationMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomVoiceControls(
    isListening: Boolean,
    audioAmplitude: Float,
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 音波动画背景
            if (isListening) {
                AudioWaveAnimation(
                    amplitude = audioAmplitude,
                    modifier = Modifier.size(120.dp)
                )
            }
            
            // 录音按钮
            val buttonScale by animateFloatAsState(
                targetValue = if (isListening) 0.9f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            
            IconButton(
                onClick = {
                    if (isListening) {
                        onStopListening()
                    } else {
                        onStartListening()
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        if (isListening) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                enabled = voiceState !is VoiceState.Processing
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "停止录音" else "开始录音",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 提示文字
            Text(
                text = when {
                    isListening -> "正在录音..."
                    voiceState is VoiceState.Processing -> "处理中..."
                    else -> "点击说话"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 100.dp)
            )
        }
    }
}

@Composable
private fun AudioWaveAnimation(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f + amplitude * 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f + amplitude * 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(modifier = modifier) {
        // 外圈
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale2)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha1 * 0.5f)
                )
        )
        
        // 内圈
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale1)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha1)
                )
        )
    }
}

@Composable
private fun ProcessingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}小时前"
        else -> "${diff / 86400_000}天前"
    }
}