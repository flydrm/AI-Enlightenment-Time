package com.enlightenment.presentation.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.enlightenment.presentation.navigation.Screen
import com.enlightenment.presentation.ui.components.FeatureCard
import com.enlightenment.presentation.ui.components.PandaMascot
import com.enlightenment.presentation.ui.components.StoryCard
import com.enlightenment.presentation.ui.theme.*



@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = remember { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.selectedFeature) {
        when (uiState.selectedFeature) {
            HomeFeature.STORY -> navController.navigate(Screen.Story.route)
            HomeFeature.CAMERA -> navController.navigate(Screen.Camera.route)
            HomeFeature.VOICE -> navController.navigate(Screen.Voice.route)
            HomeFeature.ACHIEVEMENT -> navController.navigate(Screen.Achievement.route)
            null -> {}
        }
    }
    
    HomeContent(
        uiState = uiState,
        onFeatureClick = viewModel::onFeatureClick
    )
}
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onFeatureClick: (HomeFeature) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with greeting and mascot
            HomeHeader(
                greeting = uiState.greeting,
                streak = uiState.userProgress?.currentStreak ?: 0
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Feature Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    FeatureCard(
                        title = "听故事",
                        emoji = "📚",
                        backgroundColor = SoftRed,
                        onClick = { onFeatureClick(HomeFeature.STORY) }
                    )
                }
                
                item {
                    FeatureCard(
                        title = "拍照识物",
                        emoji = "📷",
                        backgroundColor = SkyBlue,
                        onClick = { onFeatureClick(HomeFeature.CAMERA) }
                    )
                }
                
                item {
                    FeatureCard(
                        title = "语音对话",
                        emoji = "🎤",
                        backgroundColor = GrassGreen,
                        onClick = { onFeatureClick(HomeFeature.VOICE) }
                    )
                }
                
                item {
                    FeatureCard(
                        title = "我的成就",
                        emoji = "🏆",
                        backgroundColor = SunYellow,
                        onClick = { onFeatureClick(HomeFeature.ACHIEVEMENT) }
                    )
                }
            }
            
            // Recent Stories Section
            if (uiState.recentStories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                RecentStoriesSection(
                    stories = uiState.recentStories,
                    onStoryClick = { story ->
                        // Navigate to story detail
                    }
                )
            }
        }
        
        // "Loading" indicator
        if (uiState.is"Loading") {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryRed
            )
        }
    }
}
@Composable
private fun HomeHeader(
    greeting: String,
    streak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            if (streak > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔥 连续学习 $streak 天",
                    style = MaterialTheme.typography.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Panda Mascot
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LightRed)
        ) {
            PandaMascot(
                mood = PandaMood.HAPPY,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
@Composable
private fun RecentStoriesSection(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    Column {
        Text(
            text = "最近的故事",
            style = MaterialTheme.typography.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stories.take(3).forEach { story ->
                StoryCard(
                    story = story,
                    onClick = { onStoryClick(story) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
