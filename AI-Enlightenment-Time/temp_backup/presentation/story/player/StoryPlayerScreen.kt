package com.enlightenment.presentation.story.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.enlightenment.presentation.components.AnimatedPanda
import kotlinx.coroutines.delay

/**
 * 故事播放器界面
 */
@Composable
fun StoryPlayerScreen(
    storyId: String,
    onNavigateBack: () -> Unit,
    viewModel: StoryPlayerViewModel = hiltViewModel()
) {
    val story by viewModel.story.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val isAutoPlay by viewModel.isAutoPlay.collectAsStateWithLifecycle()
    
    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }
    
    story?.let { currentStory ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部栏
                StoryPlayerTopBar(
                    title = currentStory.title,
                    onNavigateBack = onNavigateBack,
                    isAutoPlay = isAutoPlay,
                    onToggleAutoPlay = viewModel::toggleAutoPlay
                )
                
                // 故事内容区
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    StoryContent(
                        story = currentStory,
                        currentChapterIndex = currentChapterIndex,
                        playerState = playerState
                    )
                    
                    // 小熊猫讲解员
                    AnimatedPanda(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(80.dp),
                        isActive = playerState is PlayerState.Playing,
                        speech = when (playerState) {
                            is PlayerState.Playing -> "听我讲故事..."
                            is PlayerState.Paused -> "暂停一下"
                            is PlayerState.Finished -> "故事讲完啦！"
                            else -> null
                        }
                    )
                }
                
                // 播放控制区
                StoryPlayerControls(
                    story = currentStory,
                    currentChapterIndex = currentChapterIndex,
                    playerState = playerState,
                    playbackProgress = playbackProgress,
                    onPlayPause = viewModel::togglePlayPause,
                    onPrevious = viewModel::previousChapter,
                    onNext = viewModel::nextChapter,
                    onSeek = viewModel::seekTo,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    } ?: run {
        // 加载中状态
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoryPlayerTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    isAutoPlay: Boolean,
    onToggleAutoPlay: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            // 自动播放开关
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "自动播放",
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = isAutoPlay,
                    onCheckedChange = { onToggleAutoPlay() },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun StoryContent(
    story: Story,
    currentChapterIndex: Int,
    playerState: PlayerState
) {
    val scrollState = rememberScrollState()
    val currentChapter = story.chapters.getOrNull(currentChapterIndex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 故事插图
        currentChapter?.imageUrl?.let { imageUrl ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "故事插图",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 章节标题
        currentChapter?.let { chapter ->
            Text(
                text = "第 ${currentChapterIndex + 1} 章",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // 故事文本
        AnimatedContent(
            targetState = currentChapter?.content ?: "",
            transitionSpec = {
                fadeIn() with fadeOut()
            }
        ) { content ->
            StoryText(
                text = content,
                isPlaying = playerState is PlayerState.Playing
            )
        }
        
        // 选择按钮（如果有）
        currentChapter?.choices?.takeIf { it.isNotEmpty() }?.let { choices ->
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "接下来想听什么？",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            choices.forEach { choice ->
                OutlinedButton(
                    onClick = { viewModel.handleChoice(choice) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = playerState !is PlayerState.Loading
                ) {
                    Text(text = choice)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StoryText(
    text: String,
    isPlaying: Boolean
) {
    var visibleText by remember(text) { mutableStateOf("") }
    
    // 文字逐字显示动画
    LaunchedEffect(text, isPlaying) {
        if (isPlaying) {
            visibleText = ""
            text.forEachIndexed { index, _ ->
                visibleText = text.substring(0, index + 1)
                delay(50) // 每个字符50ms的延迟
            }
        } else {
            visibleText = text
        }
    }
    
    Text(
        text = visibleText,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            lineHeight = 28.sp
        ),
        textAlign = TextAlign.Justify,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StoryPlayerControls(
    story: Story,
    currentChapterIndex: Int,
    playerState: PlayerState,
    playbackProgress: Float,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 进度条
            Column {
                LinearProgressIndicator(
                    progress = playbackProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "章节 ${currentChapterIndex + 1}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "共 ${story.chapters.size} 章",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 播放控制按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一章按钮
                IconButton(
                    onClick = onPrevious,
                    enabled = currentChapterIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "上一章",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 播放/暂停按钮
                val playPauseScale by animateFloatAsState(
                    targetValue = if (playerState is PlayerState.Playing) 0.9f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(playPauseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (playerState is PlayerState.Playing) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = if (playerState is PlayerState.Playing) "暂停" else "播放",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 下一章按钮
                IconButton(
                    onClick = onNext,
                    enabled = currentChapterIndex < story.chapters.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "下一章",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * 故事数据类（临时定义，应该从domain层获取）
 */
data class Story(
    val id: String,
    val title: String,
    val chapters: List<StoryChapter>
)

data class StoryChapter(
    val id: String,
    val content: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val choices: List<String> = emptyList()
)