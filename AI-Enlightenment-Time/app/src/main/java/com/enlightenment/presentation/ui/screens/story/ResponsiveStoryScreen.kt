package com.enlightenment.presentation.ui.screens.story

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.enlightenment.domain.model.Story
import com.enlightenment.presentation.navigation.Screen
import com.enlightenment.presentation.ui.responsive.*







/**
 * 响应式故事列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveStoryScreen(
    navController: NavController,
    viewModel: StoryViewModel = remember { HomeViewModel() }
) {
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val is"Loading" by viewModel.is"Loading".collectAsStateWithLifecycle()
    val responsiveSizes = rememberResponsiveSizes()
    
    Scaffold(
        topBar = {
            ResponsiveStoryTopBar(
                windowSizeClass = responsiveSizes.windowSizeClass,
                onNavigateBack = { navController.navigateUp() },
                onCreateStory = { viewModel.generateNewStory() }
            )
        },
        floatingActionButton = {
            if (responsiveSizes.windowSizeClass == WindowSizeClass.COMPACT) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.generateNewStory() },
                    icon = { Icon(Icons.Default.Add, contentDescription = "创建新故事") },
                    text = { Text("新故事") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                is"Loading" -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                stories.isEmpty() -> {
                    EmptyStoryState(
                        onCreateStory = { viewModel.generateNewStory() }
                    )
                }
                else -> {
                    when (responsiveSizes.windowSizeClass) {
                        WindowSizeClass.COMPACT -> CompactStoryList(
                            stories = stories,
                            onStoryClick = { story ->
                                navController.navigate(
                                    Screen.StoryPlayer.createRoute(story.id)
                                )
                            }
                        )
                        WindowSizeClass.MEDIUM -> MediumStoryGrid(
                            stories = stories,
                            onStoryClick = { story ->
                                navController.navigate(
                                    Screen.StoryPlayer.createRoute(story.id)
                                )
                            }
                        )
                        WindowSizeClass.EXPANDED -> ExpandedStoryLayout(
                            stories = stories,
                            onStoryClick = { story ->
                                navController.navigate(
                                    Screen.StoryPlayer.createRoute(story.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
/**
 * 响应式顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResponsiveStoryTopBar(
    windowSizeClass: WindowSizeClass,
    onNavigateBack: () -> Unit,
    onCreateStory: () -> Unit
) {
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> {
            TopAppBar(
                title = { Text("故事世界") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
        WindowSizeClass.MEDIUM, WindowSizeClass.EXPANDED -> {
            CenterAlignedTopAppBar(
                title = { Text("故事世界", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onCreateStory,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("创建新故事")
                    }
                }
            )
        }
    }
}
/**
 * 紧凑布局的故事列表
 */
@Composable
private fun CompactStoryList(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            CompactStoryCard(
                story = story,
                onClick = { onStoryClick(story) }
            )
        }
    }
}
/**
 * 中等布局的故事网格
 */
@Composable
private fun MediumStoryGrid(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stories) { story ->
            MediumStoryCard(
                story = story,
                onClick = { onStoryClick(story) }
            )
        }
    }
}
/**
 * 展开布局的故事布局
 */
@Composable
private fun ExpandedStoryLayout(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // 左侧：故事列表
        LazyColumn(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story ->
                ExpandedStoryListItem(
                    story = story,
                    onClick = { onStoryClick(story) }
                )
            }
        }
        
        // 右侧：精选故事展示
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            Text(
                text = "精选故事",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stories.take(4)) { story ->
                    ExpandedStoryCard(
                        story = story,
                        onClick = { onStoryClick(story) }
                    )
                }
            }
        }
    }
}
/**
 * 紧凑布局的故事卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactStoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 封面图
            if (story.coverImage != null) {
                AsyncImage(
                    model = story.coverImage,
                    contentDescription = story.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 故事信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = story.content.take(100) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(story.genre.name) },
                        modifier = Modifier.height(24.dp)
                    )
                    
                    Text(
                        text = "${story.readTime}分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
/**
 * 中等布局的故事卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediumStoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 封面图
            if (story.coverImage != null) {
                AsyncImage(
                    model = story.coverImage,
                    contentDescription = story.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 故事信息
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = story.content.take(150) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(story.genre.name) }
                    )
                    
                    Text(
                        text = "${story.readTime}分钟",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
/**
 * 展开布局的故事列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedStoryListItem(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(story.title) },
            supportingContent = { 
                Text(
                    story.content.take(100) + "...",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                if (story.coverImage != null) {
                    AsyncImage(
                        model = story.coverImage,
                        contentDescription = story.title,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${story.readTime}分钟",
                        style = MaterialTheme.typography.labelSmall
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(story.genre.name) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        )
    }
}
/**
 * 展开布局的故事卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedStoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 封面图
            if (story.coverImage != null) {
                AsyncImage(
                    model = story.coverImage,
                    contentDescription = story.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 故事信息
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(story.genre.name) }
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${story.readTime}分钟",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
/**
 * 空状态
 */
@Composable
private fun EmptyStoryState(
    onCreateStory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "还没有故事呢",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "让小熊猫为你创作一个精彩的故事吧！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreateStory,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("创建新故事")
        }
    }
}
